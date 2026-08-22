package com.example.accounts.struts;

import com.example.accounts.domain.CustomerProfile;
import com.example.accounts.domain.Transaction;
import com.example.accounts.ejb.AccountManagementBean;
import com.example.accounts.ejb.AccountManagementLocal;
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
import java.util.List;
import java.util.Optional;

/**
 * The customer dashboard: profile, current balance, and transaction
 * history — loaded fresh on every visit rather than baked into the JSP,
 * so the record-transaction and update-profile forms actually reflect
 * what just changed. Also carries a one-shot flash message/error from
 * those forms across their post-redirect-get hop.
 */
public class DashboardAction extends Action {
    private final AccountManagementLocal accountManagement = new AccountManagementBean();
    private final TransactionServiceLocal transactionService = new TransactionServiceBean();

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        Integer accountId = session == null ? null : (Integer) session.getAttribute("accountId");
        if (accountId == null) {
            return mapping.findForward("login");
        }

        try {
            Optional<CustomerProfile> profile = accountManagement.getProfile(accountId);
            BigDecimal balance = transactionService.getCurrentBalance(accountId);
            List<Transaction> history = transactionService.getTransactionHistory(accountId);

            request.setAttribute("profile", profile.orElse(null));
            request.setAttribute("balance", balance);
            request.setAttribute("transactions", history);
            request.setAttribute("flashMessage", session.getAttribute("flashMessage"));
            request.setAttribute("flashError", session.getAttribute("flashError"));
            session.removeAttribute("flashMessage");
            session.removeAttribute("flashError");
        } catch (SQLException e) {
            throw new RuntimeException("failed to load dashboard", e);
        }

        return mapping.findForward("success");
    }
}
