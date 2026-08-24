package com.example.accounts.struts;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

/** The vendor application form: business identity (company, category) plus a contact person and login credentials. */
public class RegisterForm extends ActionForm {
    private String username;
    private String password;
    private String companyName;
    private String category;
    private String contactName;
    private String email;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if (username == null || username.isBlank()) {
            errors.add("username", new ActionMessage("error.username.required"));
        }
        if (password == null || password.isBlank()) {
            errors.add("password", new ActionMessage("error.password.required"));
        }
        if (companyName == null || companyName.isBlank()) {
            errors.add("companyName", new ActionMessage("error.companyName.required"));
        }
        if (contactName == null || contactName.isBlank()) {
            errors.add("contactName", new ActionMessage("error.contactName.required"));
        }
        if (email == null || email.isBlank()) {
            errors.add("email", new ActionMessage("error.email.required"));
        }
        return errors;
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        this.username = null;
        this.password = null;
        this.companyName = null;
        this.category = null;
        this.contactName = null;
        this.email = null;
    }
}
