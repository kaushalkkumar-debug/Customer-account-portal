package com.example.accounts.struts;

import com.example.accounts.domain.Role;
import com.example.accounts.domain.VendorAccount;
import com.example.accounts.domain.VendorItem;
import com.example.accounts.ejb.CatalogServiceBean;
import com.example.accounts.ejb.CatalogServiceLocal;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GET /catalog — admin-only. Everything procurement can currently buy,
 * across every approved vendor (VendorItemDao.findPurchasableCatalog()
 * does the approval-gate join), with each item's vendor company name
 * resolved for display. This is the actual "shop" screen the 2014-2015
 * VMS workflow centres on: browse what's on offer, then raise a PO
 * (PurchaseItemAction) against a specific item.
 */
public class CatalogAction extends Action {
    private final CatalogServiceLocal catalogService = new CatalogServiceBean();
    private final VendorManagementLocal vendorManagement = new VendorManagementBean();

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        Role role = session == null ? null : (Role) session.getAttribute("role");
        if (role != Role.ADMIN) {
            return mapping.findForward("login");
        }

        try {
            List<VendorItem> items = catalogService.getPurchasableCatalog();
            Map<Integer, String> vendorNames = new HashMap<>();
            for (VendorItem item : items) {
                if (!vendorNames.containsKey(item.getAccountId())) {
                    vendorManagement.getAccount(item.getAccountId())
                            .map(VendorAccount::getCompanyName)
                            .ifPresent(name -> vendorNames.put(item.getAccountId(), name));
                }
            }

            request.setAttribute("items", items);
            request.setAttribute("vendorNames", vendorNames);
            request.setAttribute("flashMessage", session.getAttribute("flashMessage"));
            session.removeAttribute("flashMessage");
        } catch (SQLException e) {
            throw new RuntimeException("failed to load catalog", e);
        }

        return mapping.findForward("success");
    }
}
