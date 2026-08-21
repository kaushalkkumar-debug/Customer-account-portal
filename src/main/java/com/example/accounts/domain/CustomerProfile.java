package com.example.accounts.domain;

public final class CustomerProfile {
    private final int accountId;
    private final String fullName;
    private final String email;
    private final String phone;
    private final String address;

    public CustomerProfile(int accountId, String fullName, String email, String phone, String address) {
        this.accountId = accountId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public int getAccountId() {
        return accountId;
    }

    public String getFullName() {
        return fullName;
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
