package com.example.accounts.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One line in a vendor's accounts-payable ledger. Sign convention:
 * positive = an invoice the vendor submitted (the company now owes them
 * more), negative = a payment the company made to the vendor (settles
 * what's owed). "Amount owed to this vendor" is SUM(amount) over their
 * entries — see LedgerDao.computeAmountOwed() and the README's "why the
 * balance isn't a column".
 */
public final class LedgerEntry {
    private final int id;
    private final int accountId;
    private final BigDecimal amount;
    private final String description;
    private final LocalDateTime occurredAt;

    public LedgerEntry(int id, int accountId, BigDecimal amount, String description, LocalDateTime occurredAt) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.description = description;
        this.occurredAt = occurredAt;
    }

    public int getId() {
        return id;
    }

    public int getAccountId() {
        return accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    /** For the JSP: label an entry as an invoice or a payment without exposing the raw sign in the UI. */
    public boolean isInvoice() {
        return amount.signum() > 0;
    }
}
