package com.example.accounts.domain;

/**
 * Vendor onboarding gate — the thing that actually makes this a *vendor*
 * management system rather than a relabeled customer one. A customer
 * account is usable the moment it's created; a vendor account isn't —
 * procurement has to vet who's allowed to invoice the company before
 * they can. See VendorManagementBean.registerVendor() (always starts
 * PENDING) and SubmitInvoiceAction (refuses to accept an invoice from
 * anything but an APPROVED vendor).
 */
public enum ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED
}
