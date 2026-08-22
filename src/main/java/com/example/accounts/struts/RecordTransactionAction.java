package com.example.accounts.struts;

import com.example.accounts.ejb.TransactionServiceBean;
import com.example.accounts.ejb.TransactionServiceLocal;

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
 * POST /dashboard/recordTransaction — amount, description. Deliberately
 * validates in the Action rather than an ActionForm.validate(), so a bad
 * amount just bounces the customer back to a freshly-reloaded dashboard
 * with an error, instead of the Struts framework's validation-forward
 * dance (which would need /dashboard.jsp's data reloaded anyway).
 */
public class RecordTransactionAction extends Action {
    private final TransactionServiceLocal transactionService = new TransactionServiceBean();

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
            BigDecimal amount = new BigDecimal(amountParam.trim());
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("description is required");
            }
            transactionService.recordTransaction(accountId, amount, description.trim());
            session.setAttribute("flashMessage", "Transaction recorded.");
        } catch (IllegalArgumentException | NullPointerException e) {
            session.setAttribute("flashError", "Enter a valid amount (e.g. 25.00 or -25.00) and a description.");
        } catch (SQLException e) {
            throw new RuntimeException("failed to record transaction", e);
        }

        return mapping.findForward("success");
    }
}
