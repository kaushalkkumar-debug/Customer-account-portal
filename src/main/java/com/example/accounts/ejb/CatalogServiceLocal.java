package com.example.accounts.ejb;

import com.example.accounts.domain.ItemCategory;
import com.example.accounts.domain.VendorItem;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Local
public interface CatalogServiceLocal {
    /** Backs the vendor dashboard's "list a new item" form. */
    int addItem(int accountId, String name, ItemCategory category, BigDecimal unitPrice, String description) throws SQLException;

    /** A vendor's own catalog — their dashboard's "My catalog" section. */
    List<VendorItem> getVendorCatalog(int accountId) throws SQLException;

    /** Everything procurement can browse and buy — see VendorItemDao.findPurchasableCatalog() for the approval-gate join. */
    List<VendorItem> getPurchasableCatalog() throws SQLException;

    Optional<VendorItem> findItem(int itemId) throws SQLException;

    void setItemActive(int itemId, boolean active) throws SQLException;
}
