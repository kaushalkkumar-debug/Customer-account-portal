package com.example.accounts.domain;

/** The vendor's contact person — the human procurement actually emails/calls, as distinct from the company itself (VendorAccount.companyName). */
public final class VendorProfile {
    private final int accountId;
    private final String contactName;
    private final String email;
    private final String phone;
    private final String address;

    public VendorProfile(int accountId, String contactName, String email, String phone, String address) {
        this.accountId = accountId;
        this.contactName = contactName;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public int getAccountId() {
        return accountId;
    }

    public String getContactName() {
        return contactName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }
}
