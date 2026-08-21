package com.example.accounts.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Transaction {
    private final int id;
    private final int accountId;
    private final BigDecimal amount; // positive = credit, negative = debit
    private final String description;
    private final LocalDateTime occurredAt;

    public Transaction(int id, int accountId, BigDecimal amount, String description, LocalDateTime occurredAt) {
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
}
