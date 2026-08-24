package com.example.accounts.struts;

import com.example.accounts.domain.ApprovalStatus;
import com.example.accounts.domain.ItemCategory;
import com.example.accounts.domain.VendorAccount;
import com.example.accounts.ejb.CatalogServiceBean;
import com.example.accounts.ejb.CatalogServiceLocal;
import com.example.accounts.ejb.VendorManagementBean;
import com.example.accounts.ejb.VendorManagementLocal;

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
 * POST /dashboard/addItem — name, category, unitPrice, description.
 * Vendor-only, and only once APPROVED — an unvetted vendor listing
 * products procurement could browse and buy would defeat the entire
 * point of the approval gate.
 */
public class AddCatalogItemAction extends Action {
    private final VendorManagementLocal vendorManagement = new VendorManagementBean();
    private final CatalogServiceLocal catalogService = new CatalogServiceBean();

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        Integer accountId = session == null ? null : (Integer) session.getAttribute("accountId");
        if (accountId == null) {
            return mapping.findForward("login");
        }

        try {
            Optional<VendorAccount> account = vendorManagement.getAccount(accountId);
            if (account.isEmpty() || account.get().getApprovalStatus() != ApprovalStatus.APPROVED) {
                session.setAttribute("flashError", "Your vendor application must be approved before you can list catalog items.");
                return mapping.findForward("success");
            }

            String name = request.getParameter("name");
            ItemCategory category = ItemCategory.valueOf(request.getParameter("category"));
            BigDecimal unitPrice = new BigDecimal(request.getParameter("unitPrice").trim());
            String description = request.getParameter("description");

            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("item name is required");
            }
            if (unitPrice.signum() <= 0) {
                throw new IllegalArgumentException("unit price must be positive");
            }

            catalogService.addItem(accountId, name.trim(), category, unitPrice, description);
            session.setAttribute("flashMessage", "Added \"" + name.trim() + "\" to your catalog.");
        } catch (IllegalArgumentException | NullPointerException e) {
            session.setAttribute("flashError", "Enter a name, a category, and a positive unit price.");
        } catch (SQLException e) {
            throw new RuntimeException("failed to add catalog item", e);
        }

        return mapping.findForward("success");
    }
}
