package com.example.accounts.struts;

import com.example.accounts.domain.ApprovalStatus;
import com.example.accounts.domain.Role;
import com.example.accounts.ejb.VendorManagementBean;
import com.example.accounts.ejb.VendorManagementLocal;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;

/** POST /admin/setApprovalStatus — accountId, status=APPROVED|REJECTED. Admin-only. The approve/reject decision on a pending vendor application. */
public class SetApprovalStatusAction extends Action {
    private final VendorManagementLocal vendorManagement = new VendorManagementBean();

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        Role role = session == null ? null : (Role) session.getAttribute("role");
        if (role != Role.ADMIN) {
            return mapping.findForward("login");
        }

        try {
            int accountId = Integer.parseInt(request.getParameter("accountId"));
            ApprovalStatus status = ApprovalStatus.valueOf(request.getParameter("status"));
            vendorManagement.setApprovalStatus(accountId, status);
            session.setAttribute("flashMessage", "Vendor " + accountId + " " + status.name().toLowerCase() + ".");
        } catch (IllegalArgumentException | NullPointerException e) {
            session.setAttribute("flashMessage", "Invalid vendor id or status.");
        } catch (SQLException e) {
            throw new RuntimeException("failed to update approval status", e);
        }

        return mapping.findForward("success");
    }
}
