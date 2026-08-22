package com.example.accounts.struts;

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

/** POST /admin/toggleActive — accountId, active=true|false. Admin-only. */
public class ToggleActiveAction extends Action {
    private final AccountManagementLocal accountManagement = new AccountManagementBean();

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        Role role = session == null ? null : (Role) session.getAttribute("role");
        if (role != Role.ADMIN) {
            return mapping.findForward("login");
        }

        try {
            int accountId = Integer.parseInt(request.getParameter("accountId"));
            boolean active = Boolean.parseBoolean(request.getParameter("active"));
            accountManagement.setAccountActive(accountId, active);
            session.setAttribute("flashMessage",
                    "Account " + accountId + (active ? " reactivated." : " deactivated."));
        } catch (NumberFormatException e) {
            session.setAttribute("flashMessage", "Invalid account id.");
        } catch (SQLException e) {
            throw new RuntimeException("failed to update account status", e);
        }

        return mapping.findForward("success");
    }
}
