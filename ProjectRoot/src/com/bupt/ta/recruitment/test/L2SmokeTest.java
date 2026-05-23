package com.bupt.ta.recruitment.test;

import com.bupt.ta.recruitment.model.User;
import com.bupt.ta.recruitment.ui.AdminDashboard;
import com.bupt.ta.recruitment.ui.MODashboard;
import com.bupt.ta.recruitment.ui.TADashboard;
import com.bupt.ta.recruitment.util.CsvStorage;
import com.bupt.ta.recruitment.util.DataSeeder;
import com.bupt.ta.recruitment.util.PasswordUtil;

public class L2SmokeTest {
    public static void main(String[] args) {
        DataSeeder.seedIfNeeded();

        CsvStorage<User> userStorage = new CsvStorage<>("data/users.csv", User::fromCsvRow);
        User admin = userStorage.findById("user-admin-1", User::getId);
        User mo = userStorage.findById("user-mo-1", User::getId);
        User ta = userStorage.findById("user-ta-1", User::getId);

        assertNotNull("admin", admin);
        assertNotNull("mo", mo);
        assertNotNull("ta", ta);
        assertTrue("admin password", PasswordUtil.verifyPassword("admin123", admin.getSalt(), admin.getPasswordHash()));
        assertTrue("mo password", PasswordUtil.verifyPassword("mo123", mo.getSalt(), mo.getPasswordHash()));
        assertTrue("ta password", PasswordUtil.verifyPassword("ta123", ta.getSalt(), ta.getPasswordHash()));
        assertEqual("admin role", User.UserRole.ADMIN, admin.getRole());
        assertEqual("mo role", User.UserRole.MO, mo.getRole());
        assertEqual("ta role", User.UserRole.TA, ta.getRole());

        if (AdminDashboard.class == null || MODashboard.class == null || TADashboard.class == null) {
            throw new IllegalStateException("Dashboard classes are missing");
        }

        System.out.println("L2 smoke test passed.");
    }

    private static void assertNotNull(String label, Object value) {
        if (value == null) {
            throw new IllegalStateException(label + " should not be null");
        }
    }

    private static void assertTrue(String label, boolean condition) {
        if (!condition) {
            throw new IllegalStateException(label + " should be true");
        }
    }

    private static void assertEqual(String label, Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new IllegalStateException(label + " expected " + expected + " but got " + actual);
        }
    }
}
