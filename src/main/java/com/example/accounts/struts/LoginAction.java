package com.example.accounts.struts;

import com.example.accounts.domain.CustomerAccount;
import com.example.accounts.domain.Role;
import com.example.accounts.ejb.AccountManagementBean;
import com.example.accounts.ejb.AccountManagementLocal;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Role-based secure login: authenticates via the AccountManagementBean
 * (business layer), then routes to a different forward depending on the
 * account's role — the "role-based" part of the requirement, not just
 * checking a password. Looks up the bean directly rather than via JNDI
 * (real container deployment would inject/look it up); see README
 * "About the EJB layer".
 */
public class LoginAction extends Action {
    private final AccountManagementLocal accountManagement = new AccountManagementBean();

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        LoginForm loginForm = (LoginForm) form;

        try {
            Optional<CustomerAccount> account = accountManagement.authenticate(loginForm.getUsername(), loginForm.getPassword());

            if (account.isEmpty()) {
                ActionErrors errors = new ActionErrors();
                errors.add("login", new ActionMessage("error.login.invalid"));
                saveErrors(request, errors);
                return mapping.getInputForward();
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("accountId", account.get().getId());
            session.setAttribute("role", account.get().getRole());

            return accountManagement.hasRole(account.get(), Role.ADMIN)
                    ? mapping.findForward("adminHome")
                    : mapping.findForward("customerHome");
        } catch (SQLException e) {
            throw new RuntimeException("login failed", e);
        }
    }
}
