package com.example.accounts.domain;

/** Login credentials + business identity — never carries a plaintext password, see security/PasswordHasher. */
public final class VendorAccount {
    private final int id;
    private final String username;
    private final String passwordHash;
    private final String passwordSalt;
    private final Role role;
    private final boolean active;
    private final String companyName;
    private final VendorCategory category;
    private final ApprovalStatus approvalStatus;

    public VendorAccount(int id, String username, String passwordHash, String passwordSalt, Role role, boolean active,
                          String companyName, VendorCategory category, ApprovalStatus approvalStatus) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.role = role;
        this.active = active;
        this.companyName = companyName;
        this.category = category;
        this.approvalStatus = approvalStatus;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public String getCompanyName() {
        return companyName;
    }

    public VendorCategory getCategory() {
        return category;
    }

    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public boolean isApproved() {
        return approvalStatus == ApprovalStatus.APPROVED;
    }
}
