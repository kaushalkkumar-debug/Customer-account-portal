package com.example.accounts.struts;

import com.example.accounts.domain.ApprovalStatus;
import com.example.accounts.domain.Role;
import com.example.accounts.domain.VendorAccount;
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
import java.util.ArrayList;
import java.util.List;

/**
 * The admin console: a pending-applications queue (approve/reject) split
 * out from the roster of already-decided vendors (deactivate/reactivate,
 * record a payment). One DAO call (findAllAccounts), split in the
 * controller by approvalStatus rather than two separate queries — the
 * "pending" list is usually tiny, so filtering in memory here is simpler
 * than a second round trip.
 */
public class AdminAction extends Action {
    private final VendorManagementLocal vendorManagement = new VendorManagementBean();

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        Role role = session == null ? null : (Role) session.getAttribute("role");
        if (role != Role.ADMIN) {
            return mapping.findForward("login");
        }

        try {
            List<VendorAccount> allAccounts = vendorManagement.findAllAccounts();
            List<VendorAccount> pending = new ArrayList<>();
            List<VendorAccount> decided = new ArrayList<>();
            for (VendorAccount account : allAccounts) {
                if (account.getApprovalStatus() == ApprovalStatus.PENDING) {
                    pending.add(account);
                } else {
                    decided.add(account);
                }
            }

            request.setAttribute("pendingVendors", pending);
            request.setAttribute("vendors", decided);
            request.setAttribute("flashMessage", session.getAttribute("flashMessage"));
            session.removeAttribute("flashMessage");
        } catch (SQLException e) {
            throw new RuntimeException("failed to load vendors", e);
        }

        return mapping.findForward("success");
    }
}
