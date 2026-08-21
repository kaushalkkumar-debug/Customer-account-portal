package com.example.accounts.domain;

/** Login credentials + role — never carries a plaintext password, see security/PasswordHasher. */
public final class CustomerAccount {
    private final int id;
    private final String username;
    private final String passwordHash;
    private final String passwordSalt;
    private final Role role;
    private final boolean active;

    public CustomerAccount(int id, String username, String passwordHash, String passwordSalt, Role role, boolean active) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.role = role;
        this.active = active;
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
}
