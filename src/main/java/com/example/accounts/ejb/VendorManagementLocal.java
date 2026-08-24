package com.example.accounts.ejb;

import com.example.accounts.domain.ApprovalStatus;
import com.example.accounts.domain.Role;
import com.example.accounts.domain.VendorAccount;
import com.example.accounts.domain.VendorCategory;
import com.example.accounts.domain.VendorProfile;

import javax.ejb.Local;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Local
public interface VendorManagementLocal {
    /** Self-service application: always starts ApprovalStatus.PENDING — see README "The approval workflow". */
    int registerVendor(String username, String plaintextPassword, String companyName, VendorCategory category,
                        String contactName, String email) throws SQLException;

    /** Empty when the username doesn't exist, the account is deactivated, or the password is wrong — never distinguishes which, to avoid leaking account existence. Deliberately does NOT gate on approval status: a pending vendor can still log in to see their application status. */
    Optional<VendorAccount> authenticate(String username, String plaintextPassword) throws SQLException;

    boolean hasRole(VendorAccount account, Role required);

    void deactivateAccount(int accountId) throws SQLException;

    Optional<VendorAccount> getAccount(int accountId) throws SQLException;

    /** Backs the vendor dashboard's profile panel. */
    Optional<VendorProfile> getProfile(int accountId) throws SQLException;

    /** Backs the dashboard's "update contact details" form. */
    void updateProfile(int accountId, String phone, String address) throws SQLException;

    /** Backs the admin console — every vendor, pending and approved alike; the caller splits by approval status. */
    List<VendorAccount> findAllAccounts() throws SQLException;

    /** Generalizes deactivateAccount() to support reactivating too. */
    void setAccountActive(int accountId, boolean active) throws SQLException;

    /** Procurement's approve/reject decision on a pending vendor application. */
    void setApprovalStatus(int accountId, ApprovalStatus status) throws SQLException;
}
