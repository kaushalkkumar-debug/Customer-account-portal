package com.example.accounts.domain;

import java.math.BigDecimal;

/**
 * One catalog listing: a specific product a vendor sells (a software
 * licence, a model of laptop, an IoT sensor kit...) at a unit price.
 * Procurement raises a purchase order against one of these — see
 * PurchaseItemAction — which is what actually drives the vendor's AP
 * ledger, not the vendor self-reporting invoices.
 */
public final class VendorItem {
    private final int id;
    private final int accountId;
    private final String name;
    private final ItemCategory category;
    private final BigDecimal unitPrice;
    private final String description;
    private final boolean active;

    public VendorItem(int id, int accountId, String name, ItemCategory category, BigDecimal unitPrice, String description, boolean active) {
        this.id = id;
        this.accountId = accountId;
        this.name = name;
        this.category = category;
        this.unitPrice = unitPrice;
        this.description = description;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public int getAccountId() {
        return accountId;
    }

    public String getName() {
        return name;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }
}
