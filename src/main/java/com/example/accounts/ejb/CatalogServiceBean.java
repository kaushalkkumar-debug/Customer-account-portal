package com.example.accounts.ejb;

import com.example.accounts.dao.VendorItemDao;
import com.example.accounts.db.DataSourceConfig;
import com.example.accounts.db.SchemaInitializer;
import com.example.accounts.domain.ItemCategory;
import com.example.accounts.domain.VendorItem;

import javax.ejb.Stateless;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** The vendor catalog business layer — what each vendor sells, and what procurement can currently buy. */
@Stateless
public class CatalogServiceBean implements CatalogServiceLocal {

    @Override
    public int addItem(int accountId, String name, ItemCategory category, BigDecimal unitPrice, String description) throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            return new VendorItemDao(conn).create(accountId, name, category, unitPrice, description);
        }
    }

    @Override
    public List<VendorItem> getVendorCatalog(int accountId) throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            return new VendorItemDao(conn).findByVendor(accountId);
        }
    }

    @Override
    public List<VendorItem> getPurchasableCatalog() throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            return new VendorItemDao(conn).findPurchasableCatalog();
        }
    }

    @Override
    public Optional<VendorItem> findItem(int itemId) throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            return new VendorItemDao(conn).findById(itemId);
        }
    }

    @Override
    public void setItemActive(int itemId, boolean active) throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            new VendorItemDao(conn).setActive(itemId, active);
        }
    }
}
