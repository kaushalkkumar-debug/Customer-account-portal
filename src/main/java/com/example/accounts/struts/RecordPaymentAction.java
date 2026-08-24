package com.example.accounts.struts;

import com.example.accounts.domain.Role;
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

/**
 * POST /admin/recordPayment — vendorId, amount (entered positive, e.g.
 * "settling £500"), description. Admin-only. Stores it as a NEGATIVE
 * ledger entry — the sign flip is deliberate and happens here, not on the
 * form: an admin thinks in terms of "how much am I paying this vendor",
 * not "what sign does the ledger convention use".
 */
public class RecordPaymentAction extends Action {
    private final LedgerServiceLocal ledgerService = new LedgerServiceBean();

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        Role role = session == null ? null : (Role) session.getAttribute("role");
        if (role != Role.ADMIN) {
            return mapping.findForward("login");
        }

        String vendorIdParam = request.getParameter("vendorId");
        String amountParam = request.getParameter("amount");
        String description = request.getParameter("description");

        try {
            int vendorId = Integer.parseInt(vendorIdParam);
            BigDecimal paymentAmount = new BigDecimal(amountParam.trim());
            if (paymentAmount.signum() <= 0) {
                throw new IllegalArgumentException("a payment amount must be positive");
            }
            String note = (description == null || description.isBlank()) ? "Payment" : description.trim();
            ledgerService.recordEntry(vendorId, paymentAmount.negate(), note);
            session.setAttribute("flashMessage", "Payment of " + paymentAmount + " recorded for vendor " + vendorId + ".");
        } catch (IllegalArgumentException e) {
            session.setAttribute("flashMessage", "Enter a valid vendor id and a positive payment amount.");
        } catch (SQLException e) {
            throw new RuntimeException("failed to record payment", e);
        }

        return mapping.findForward("success");
    }
}
