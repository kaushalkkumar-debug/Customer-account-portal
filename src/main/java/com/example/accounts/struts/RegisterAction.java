package com.example.accounts.struts;

import com.example.accounts.domain.VendorCategory;
import com.example.accounts.ejb.VendorManagementBean;
import com.example.accounts.ejb.VendorManagementLocal;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;

/**
 * POST /register — a prospective vendor's application. Always creates the
 * account with ApprovalStatus.PENDING (VendorManagementBean.registerVendor
 * enforces this regardless of what's posted); the applicant is sent back
 * to the login screen with a note that it's awaiting review, not straight
 * into a dashboard.
 */
public class RegisterAction extends Action {
    private final VendorManagementLocal vendorManagement = new VendorManagementBean();

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        RegisterForm registerForm = (RegisterForm) form;

        try {
            VendorCategory category = null;
            if (registerForm.getCategory() != null && !registerForm.getCategory().isBlank()) {
                category = VendorCategory.valueOf(registerForm.getCategory());
            }
            vendorManagement.registerVendor(
                    registerForm.getUsername(), registerForm.getPassword(),
                    registerForm.getCompanyName(), category,
                    registerForm.getContactName(), registerForm.getEmail());

            request.getSession(true).setAttribute("flashMessage",
                    "Application submitted. A procurement admin will review " + registerForm.getCompanyName() + " shortly — you can log in now to check its status.");
            return mapping.findForward("success");
        } catch (IllegalStateException e) {
            ActionErrors errors = new ActionErrors();
            errors.add("username", new ActionMessage("error.username.taken"));
            saveErrors(request, errors);
            return mapping.getInputForward();
        } catch (SQLException e) {
            throw new RuntimeException("registration failed", e);
        }
    }
}
