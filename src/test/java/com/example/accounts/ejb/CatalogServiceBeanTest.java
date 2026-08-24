package com.example.accounts.ejb;

import com.example.accounts.dao.VendorDao;
import com.example.accounts.db.DataSourceConfig;
import com.example.accounts.db.SchemaInitializer;
import com.example.accounts.domain.ApprovalStatus;
import com.example.accounts.domain.ItemCategory;
import com.example.accounts.domain.Role;
import com.example.accounts.domain.VendorCategory;
import com.example.accounts.domain.VendorItem;
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

class CatalogServiceBeanTest {

    private static int seedVendor(ApprovalStatus status, boolean active) throws SQLException {
        try (Connection conn = DataSourceConfig.getConnection()) {
            SchemaInitializer.ensureSchema(conn);
            String salt = PasswordHasher.generateSalt();
            int id = new VendorDao(conn).create("cat-vendor-" + UUID.randomUUID(),
                    PasswordHasher.hash("pw", salt), salt, Role.VENDOR,
                    "Catalog Test Co", VendorCategory.SUPPLIES, status);
            if (!active) {
                new VendorDao(conn).setActive(id, false);
            }
            return id;
        }
    }

    @Test
    void addItemThenGetVendorCatalogReturnsIt() throws SQLException {
        int vendorId = seedVendor(ApprovalStatus.APPROVED, true);
        CatalogServiceLocal service = new CatalogServiceBean();

        service.addItem(vendorId, "ThinkPad X1 Carbon", ItemCategory.LAPTOP, new BigDecimal("1450.00"), "14in business laptop");

        List<VendorItem> catalog = service.getVendorCatalog(vendorId);
        assertEquals(1, catalog.size());
        assertEquals("ThinkPad X1 Carbon", catalog.get(0).getName());
        assertEquals(ItemCategory.LAPTOP, catalog.get(0).getCategory());
    }

    @Test
    void purchasableCatalogOnlyIncludesApprovedActiveVendors() throws SQLException {
        int approvedVendor = seedVendor(ApprovalStatus.APPROVED, true);
        int pendingVendor = seedVendor(ApprovalStatus.PENDING, true);
        int deactivatedVendor = seedVendor(ApprovalStatus.APPROVED, false);
        CatalogServiceLocal service = new CatalogServiceBean();

        service.addItem(approvedVendor, "Approved Vendor's Widget", ItemCategory.HARDWARE, new BigDecimal("10.00"), null);
        service.addItem(pendingVendor, "Pending Vendor's Widget", ItemCategory.HARDWARE, new BigDecimal("10.00"), null);
        service.addItem(deactivatedVendor, "Deactivated Vendor's Widget", ItemCategory.HARDWARE, new BigDecimal("10.00"), null);

        List<VendorItem> purchasable = service.getPurchasableCatalog();

        assertTrue(purchasable.stream().anyMatch(i -> i.getName().equals("Approved Vendor's Widget")));
        assertFalse(purchasable.stream().anyMatch(i -> i.getName().equals("Pending Vendor's Widget")),
                "a pending vendor's items must not be purchasable");
        assertFalse(purchasable.stream().anyMatch(i -> i.getName().equals("Deactivated Vendor's Widget")),
                "a deactivated vendor's items must not be purchasable");
    }

    @Test
    void setItemActiveRemovesItFromThePurchasableCatalog() throws SQLException {
        int vendorId = seedVendor(ApprovalStatus.APPROVED, true);
        CatalogServiceLocal service = new CatalogServiceBean();
        int itemId = service.addItem(vendorId, "Delistable Item", ItemCategory.SOFTWARE, new BigDecimal("99.00"), null);

        service.setItemActive(itemId, false);

        List<VendorItem> purchasable = service.getPurchasableCatalog();
        assertFalse(purchasable.stream().anyMatch(i -> i.getId() == itemId));
    }

    @Test
    void findItemReturnsItsCurrentUnitPrice() throws SQLException {
        int vendorId = seedVendor(ApprovalStatus.APPROVED, true);
        CatalogServiceLocal service = new CatalogServiceBean();
        int itemId = service.addItem(vendorId, "IoT Sensor Kit", ItemCategory.IOT_DEVICE, new BigDecimal("249.99"), "10-pack temperature sensors");

        VendorItem item = service.findItem(itemId).orElseThrow();

        assertEquals(0, item.getUnitPrice().compareTo(new BigDecimal("249.99")));
    }
}
