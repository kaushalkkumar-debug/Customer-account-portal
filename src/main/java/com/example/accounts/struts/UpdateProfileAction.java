package com.example.accounts.struts;

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

/** POST /dashboard/updateProfile — phone, address. */
public class UpdateProfileAction extends Action {
    private final AccountManagementLocal accountManagement = new AccountManagementBean();

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        Integer accountId = session == null ? null : (Integer) session.getAttribute("accountId");
        if (accountId == null) {
            return mapping.findForward("login");
        }

        String phone = request.getParameter("phone");
        String address = request.getParameter("address");

        try {
            accountManagement.updateProfile(accountId, phone, address);
            session.setAttribute("flashMessage", "Contact details updated.");
        } catch (SQLException e) {
            throw new RuntimeException("failed to update profile", e);
        }

        return mapping.findForward("success");
    }
}
