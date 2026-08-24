package com.example.accounts.struts;

import com.example.accounts.domain.LedgerEntry;
import com.example.accounts.domain.VendorAccount;
import com.example.accounts.domain.VendorItem;
import com.example.accounts.domain.VendorProfile;
import com.example.accounts.ejb.CatalogServiceBean;
import com.example.accounts.ejb.CatalogServiceLocal;
import com.example.accounts.ejb.VendorManagementBean;
import com.example.accounts.ejb.VendorManagementLocal;
import com.example.accounts.ejb.LedgerServiceBean;
import com.example.accounts.ejb.LedgerServiceLocal;

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
 * The vendor dashboard: business identity + approval status, current
 * amount owed, ledger history, and (once APPROVED) their catalog and an
 * invoice-submission form. Loaded fresh on every visit rather than baked
 * into the JSP, so the catalog/invoice/profile forms actually reflect
 * what just changed. Also carries a one-shot flash message/error from
 * those forms across their post-redirect-get hop.
 */
public class DashboardAction extends Action {
    private final VendorManagementLocal vendorManagement = new VendorManagementBean();
    private final LedgerServiceLocal ledgerService = new LedgerServiceBean();
    private final CatalogServiceLocal catalogService = new CatalogServiceBean();

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        Integer accountId = session == null ? null : (Integer) session.getAttribute("accountId");
        if (accountId == null) {
            return mapping.findForward("login");
        }

        try {
            Optional<VendorAccount> account = vendorManagement.getAccount(accountId);
            Optional<VendorProfile> profile = vendorManagement.getProfile(accountId);
            BigDecimal amountOwed = ledgerService.getAmountOwed(accountId);
            List<LedgerEntry> history = ledgerService.getLedgerHistory(accountId);
            List<VendorItem> catalog = catalogService.getVendorCatalog(accountId);

            request.setAttribute("account", account.orElse(null));
            request.setAttribute("profile", profile.orElse(null));
            request.setAttribute("amountOwed", amountOwed);
            request.setAttribute("ledgerEntries", history);
            request.setAttribute("catalog", catalog);
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
