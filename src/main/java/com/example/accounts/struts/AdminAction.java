package com.example.accounts.struts;

import com.example.accounts.domain.CustomerAccount;
import com.example.accounts.domain.Role;
import com.example.accounts.ejb.AccountManagementBean;
import com.example.accounts.ejb.AccountManagementLocal;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.List;

/** The admin account-management screen: every customer account, with a deactivate/reactivate toggle per row. */
public class AdminAction extends Action {
    private final AccountManagementLocal accountManagement = new AccountManagementBean();

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        Role role = session == null ? null : (Role) session.getAttribute("role");
        if (role != Role.ADMIN) {
            return mapping.findForward("login");
        }

        try {
            List<CustomerAccount> accounts = accountManagement.findAllAccounts();
            request.setAttribute("accounts", accounts);
            request.setAttribute("flashMessage", session.getAttribute("flashMessage"));
            session.removeAttribute("flashMessage");
        } catch (SQLException e) {
            throw new RuntimeException("failed to load accounts", e);
        }

        return mapping.findForward("success");
    }
}
