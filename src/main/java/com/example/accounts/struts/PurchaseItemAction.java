package com.example.accounts.struts;

import com.example.accounts.domain.Role;
import com.example.accounts.domain.VendorItem;
import com.example.accounts.ejb.CatalogServiceBean;
import com.example.accounts.ejb.CatalogServiceLocal;
import com.example.accounts.ejb.LedgerServiceBean;
import com.example.accounts.ejb.LedgerServiceLocal;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

/**
 * POST /catalog/purchase — itemId, quantity. Admin-only. This is what
 * actually drives a vendor's amount-owed in this workflow: procurement
 * raises a purchase order against a catalog item, quantity times the
 * item's unit price becomes a positive ledger entry against that vendor
 * (an invoice, in ledger terms — see LedgerEntry's sign convention),
 * exactly as if the vendor had invoiced for it themselves. The unit price
 * is read from the catalog record at purchase time, not trusted from the
 * request — a tampered/stale price in a hidden form field can't change
 * what actually gets billed.
 */
public class PurchaseItemAction extends Action {
    private final CatalogServiceLocal catalogService = new CatalogServiceBean();
    private final LedgerServiceLocal ledgerService = new LedgerServiceBean();

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        Role role = session == null ? null : (Role) session.getAttribute("role");
        if (role != Role.ADMIN) {
            return mapping.findForward("login");
        }

        try {
            int itemId = Integer.parseInt(request.getParameter("itemId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be positive");
            }

            Optional<VendorItem> item = catalogService.findItem(itemId);
            if (item.isEmpty() || !item.get().isActive()) {
                session.setAttribute("flashMessage", "That item is no longer available.");
                return mapping.findForward("success");
            }

            VendorItem catalogItem = item.get();
            BigDecimal total = catalogItem.getUnitPrice().multiply(BigDecimal.valueOf(quantity));
            String description = "PO: " + quantity + "x " + catalogItem.getName();
            ledgerService.recordEntry(catalogItem.getAccountId(), total, description);

            session.setAttribute("flashMessage",
                    "Purchase order raised: " + quantity + "x " + catalogItem.getName() + " (" + total + ").");
        } catch (IllegalArgumentException e) {
            session.setAttribute("flashMessage", "Enter a valid item and a positive quantity.");
        } catch (SQLException e) {
            throw new RuntimeException("failed to raise purchase order", e);
        }

        return mapping.findForward("success");
    }
}
