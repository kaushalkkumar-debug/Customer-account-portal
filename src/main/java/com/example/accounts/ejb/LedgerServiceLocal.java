package com.example.accounts.ejb;

import com.example.accounts.domain.LedgerEntry;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

@Local
public interface LedgerServiceLocal {
    /** Positive amount = an invoice; negative = a payment made to the vendor. Struts callers (SubmitInvoiceAction, RecordPaymentAction) enforce the sign for each case — see README "Why the balance isn't a column". */
    int recordEntry(int accountId, BigDecimal amount, String description) throws SQLException;

    List<LedgerEntry> getLedgerHistory(int accountId) throws SQLException;

    BigDecimal getAmountOwed(int accountId) throws SQLException;
}
