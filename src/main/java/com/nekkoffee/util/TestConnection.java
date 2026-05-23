package com.nekkoffee.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestConnection {
    public static void main(String[] args) {
        System.out.println("=== NEKKOFFEE DATABASE CONNECTION TEST ===");

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ SUCCESS: Connected to nekoffeedb successfully!");

                // Let's run a quick query to test if your tables exist
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM product")) {

                    if (rs.next()) {
                        System.out.println("📊 DATABASE CHECK: 'product' table found with "
                                + rs.getInt("total") + " items loaded.");
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ Connected to server, but failed to read tables: " + e.getMessage());
                    System.out.println("Check if you imported 'nekoffeedb.sql' inside phpMyAdmin!");
                }

            }
        } catch (Exception e) {
            System.out.println("❌ CONNECTION FAILED!");
            System.out.println("Error Details: " + e.getMessage());
            System.out.println("\n--- Troubleshooting Checklist ---");
            System.out.println("1. Is XAMPP open and both Apache & MySQL running (Green)?");
            System.out.println("2. Double check your MySQL port in XAMPP. Is it 3100 or 3306?");
            System.out.println("   (Change the URL in DatabaseConnection.java if needed)");
        }
    }
}