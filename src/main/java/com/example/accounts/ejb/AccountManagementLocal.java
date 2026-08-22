package com.example.accounts.ejb;

import com.example.accounts.domain.CustomerAccount;
import com.example.accounts.domain.CustomerProfile;
import com.example.accounts.domain.Role;

import javax.ejb.Local;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Local
public interface AccountManagementLocal {
    int registerCustomer(String username, String plaintextPassword, String fullName, String email) throws SQLException;

    /** Empty when the username doesn't exist, the account is deactivated, or the password is wrong — never distinguishes which, to avoid leaking account existence. */
    Optional<CustomerAccount> authenticate(String username, String plaintextPassword) throws SQLException;

    boolean hasRole(CustomerAccount account, Role required);

    void deactivateAccount(int accountId) throws SQLException;

    /** Backs the customer dashboard's profile panel. */
    Optional<CustomerProfile> getProfile(int accountId) throws SQLException;

    /** Backs the dashboard's "update contact details" form. */
    void updateProfile(int accountId, String phone, String address) throws SQLException;

    /** Backs the admin account-management screen. */
    List<CustomerAccount> findAllAccounts() throws SQLException;

    /** Generalizes deactivateAccount() to support reactivating too. */
    void setAccountActive(int accountId, boolean active) throws SQLException;
}
