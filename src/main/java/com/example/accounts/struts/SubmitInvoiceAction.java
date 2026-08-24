package com.example.accounts.struts;

import com.example.accounts.domain.ApprovalStatus;
import com.example.accounts.domain.VendorAccount;
import com.example.accounts.ejb.VendorManagementBean;
import com.example.accounts.ejb.VendorManagementLocal;
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
 * POST /dashboard/submitInvoice — amount, description. For a charge that
 * isn't tied to a catalog purchase order (freight, a correction, a one-off
 * service) — most of what a vendor is owed comes from procurement raising
 * POs against their catalog (see PurchaseItemAction), not from this form.
 * Two gates a PurchaseItemAction-driven entry doesn't need: the vendor
 * must be APPROVED (a PENDING vendor has nothing to invoice yet), and the
 * amount must be positive — a vendor invoicing a negative amount would be
 * paying the company, not billing it; see RecordPaymentAction for the
 * admin-only side of that. Deliberately validates in the Action rather
 * than an ActionForm.validate(), so a bad amount just bounces the vendor
 * back to a freshly-reloaded dashboard with an error, instead of the
 * Struts framework's validation-forward dance (which would need
 * dashboard.jsp's data reloaded anyway).
 */
public class SubmitInvoiceAction extends Action {
    private final VendorManagementLocal vendorManagement = new VendorManagementBean();
    private final LedgerServiceLocal ledgerService = new LedgerServiceBean();

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        Integer accountId = session == null ? null : (Integer) session.getAttribute("accountId");
        if (accountId == null) {
            return mapping.findForward("login");
        }

        String amountParam = request.getParameter("amount");
        String description = request.getParameter("description");

        try {
            Optional<VendorAccount> account = vendorManagement.getAccount(accountId);
            if (account.isEmpty() || account.get().getApprovalStatus() != ApprovalStatus.APPROVED) {
                session.setAttribute("flashError", "Your vendor application must be approved before you can submit an invoice.");
                return mapping.findForward("success");
            }

            BigDecimal amount = new BigDecimal(amountParam.trim());
            if (amount.signum() <= 0) {
                throw new IllegalArgumentException("an invoice amount must be positive");
            }
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("description is required");
            }
            ledgerService.recordEntry(accountId, amount, description.trim());
            session.setAttribute("flashMessage", "Invoice submitted.");
        } catch (IllegalArgumentException | NullPointerException e) {
            session.setAttribute("flashError", "Enter a positive invoice amount (e.g. 25.00) and a description.");
        } catch (SQLException e) {
            throw new RuntimeException("failed to submit invoice", e);
        }

        return mapping.findForward("success");
    }
}
