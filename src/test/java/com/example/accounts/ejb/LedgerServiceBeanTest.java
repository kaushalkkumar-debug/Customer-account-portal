package com.example.accounts.ejb;

import com.example.accounts.dao.VendorDao;
import com.example.accounts.db.DataSourceConfig;
import com.example.accounts.db.SchemaInitializer;
import com.example.accounts.domain.ApprovalStatus;
import com.example.accounts.domain.LedgerEntry;
import com.example.accounts.domain.Role;
import com.example.accounts.domain.VendorCategory;
import com.example.accounts.security.PasswordHasher;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LedgerServiceBeanTest {

    private static int seedVendor() throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            String salt = PasswordHasher.generateSalt();
            return new VendorDao(conn).create("ledger-vendor-" + UUID.randomUUID(),
                    PasswordHasher.hash("pw", salt), salt, Role.VENDOR,
                    "Test Vendor Co", VendorCategory.SUPPLIES, ApprovalStatus.APPROVED);
        }
    }

    @Test
    void recordedEntriesAppearInHistoryMostRecentFirst() throws SQLException {
        int accountId = seedVendor();
        LedgerServiceLocal service = new LedgerServiceBean();

        service.recordEntry(accountId, new BigDecimal("100.00"), "Invoice #1");
        service.recordEntry(accountId, new BigDecimal("-25.50"), "Payment");

        List<LedgerEntry> history = service.getLedgerHistory(accountId);

        assertEquals(2, history.size());
        assertEquals("Payment", history.get(0).getDescription(), "most recent entry should be first");
    }

    @Test
    void isInvoiceDistinguishesPositiveFromNegativeEntries() throws SQLException {
        int accountId = seedVendor();
        LedgerServiceLocal service = new LedgerServiceBean();

        service.recordEntry(accountId, new BigDecimal("100.00"), "Invoice #1");
        service.recordEntry(accountId, new BigDecimal("-25.50"), "Payment");

        List<LedgerEntry> history = service.getLedgerHistory(accountId);

        assertFalse(history.get(0).isInvoice(), "the payment should not be flagged as an invoice");
        assertTrue(history.get(1).isInvoice(), "the invoice should be flagged as an invoice");
    }

    @Test
    void amountOwedIsTheSumOfAllEntries() throws SQLException {
        int accountId = seedVendor();
        LedgerServiceLocal service = new LedgerServiceBean();

        service.recordEntry(accountId, new BigDecimal("100.00"), "PO: 1x Widget");
        service.recordEntry(accountId, new BigDecimal("-25.50"), "Payment");
        service.recordEntry(accountId, new BigDecimal("10.00"), "Correction invoice");

        BigDecimal amountOwed = service.getAmountOwed(accountId);

        assertEquals(0, amountOwed.compareTo(new BigDecimal("84.50")), "expected 84.50, was " + amountOwed);
    }

    @Test
    void amountOwedIsZeroForAVendorWithNoEntries() throws SQLException {
        int accountId = seedVendor();
        LedgerServiceLocal service = new LedgerServiceBean();

        assertEquals(0, service.getAmountOwed(accountId).compareTo(BigDecimal.ZERO));
    }

    @Test
    void historyIsScopedToItsOwnVendor() throws SQLException {
        int vendorA = seedVendor();
        int vendorB = seedVendor();
        LedgerServiceLocal service = new LedgerServiceBean();

        service.recordEntry(vendorA, new BigDecimal("50.00"), "A's invoice");
        service.recordEntry(vendorB, new BigDecimal("999.00"), "B's invoice");

        List<LedgerEntry> historyA = service.getLedgerHistory(vendorA);

        assertEquals(1, historyA.size());
        assertTrue(historyA.stream().allMatch(e -> e.getAccountId() == vendorA));
    }
}
