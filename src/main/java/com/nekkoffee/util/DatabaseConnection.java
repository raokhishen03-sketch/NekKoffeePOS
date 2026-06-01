package com.nekkoffee.util;

import com.nekkoffee.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection {
    // Make sure the port (3100 or 3306) matches whatever your XAMPP MySQL is using!
    private static final String URL = "jdbc:mysql://localhost:3306/nekoffeedb";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found!", e);
        }
    }

    /**
     * Fetches base items filtered by category. Excludes extensions/add-ons (isExtension = 0).
     */
    public static List<Product> getProductsByCategory(String categoryName) {
        List<Product> products = new ArrayList<>();
        String query;

        if (categoryName.equalsIgnoreCase("All Items")) {
            query = "SELECT * FROM product WHERE isExtension = 0";
        } else {
            query = "SELECT p.* FROM product p " +
                    "JOIN category c ON p.categoryID = c.categoryID " +
                    "WHERE c.categoryName = ? AND p.isExtension = 0";
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            if (!categoryName.equalsIgnoreCase("All Items")) {
                stmt.setString(1, categoryName);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("productID"),
                        rs.getString("productName"),
                        rs.getDouble("price"),
                        rs.getString("imagePath"),
                        rs.getBoolean("isExtension"),
                        rs.getInt("categoryID")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Fallback mock data so the UI doesn't crash if the DB server drops
            return getMockProductsByCategory(categoryName);
        }
        return products;
    }

    /**
     * Fetches only the add-ons that are legally allowed for a specific base item
     * using Member 2's 'product_restrictions' mapping table.
     */
    public static List<Product> getAddonsForProduct(int baseProductID) {
        List<Product> addons = new ArrayList<>();
        String query = "SELECT p.* FROM product p " +
                "JOIN product_restrictions pr ON p.productID = pr.addonProductID " +
                "WHERE pr.baseProductID = ? AND p.isExtension = 1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, baseProductID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                addons.add(new Product(
                        rs.getInt("productID"),
                        rs.getString("productName"),
                        rs.getDouble("price"),
                        rs.getString("imagePath"),
                        rs.getBoolean("isExtension"),
                        rs.getInt("categoryID")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return getMockAddonsForProduct(baseProductID);
        }
        return addons;
    }

    // --- FALLBACK MOCK DATA (Keeps UI running smoothly during testing) ---
    private static List<Product> getMockProductsByCategory(String categoryName) {
        List<Product> items = new ArrayList<>();
        items.add(new Product(1, "Fallback Latte (DB Offline)", 11.00, null, false, 1));
        return items;
    }

    private static List<Product> getMockAddonsForProduct(int productID) {
        List<Product> addons = new ArrayList<>();
        addons.add(new Product(99, "Fallback Extra Shot (DB Offline)", 2.00, null, true, 1));
        return addons;
    }
    /**
     * Loyalty points: add points based on bill total.
     */
    public static void addLoyaltyPoints(String phoneNumber, double billTotal) {
        try (Connection conn = getConnection()) {
            String sql = "UPDATE customers SET loyalty_points = loyalty_points + ? WHERE phone_number = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            int points = (int)(billTotal / 10); // Example: 1 point per RM10 spent
            stmt.setInt(1, points);
            stmt.setString(2, phoneNumber);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save order header into orders table.
     */
    public static int saveOrder(String serviceType, double subtotal, double tax, double total) {
        int orderId = -1;
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO orders(service_type, subtotal, tax, total) VALUES(?,?,?,?)";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, serviceType);
            stmt.setDouble(2, subtotal);
            stmt.setDouble(3, tax);
            stmt.setDouble(4, total);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                orderId = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderId;
    }

    /**
     * Save each order item into order_items table.
     */
    public static void saveOrderItem(int orderId, int productId, double price) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO order_items(order_id, product_id, price) VALUES(?,?,?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, orderId);
            stmt.setInt(2, productId);
            stmt.setDouble(3, price);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // sales history
    public static List<String> getSalesHistory() {
        List<String> history = new ArrayList<>();
        String sql = "SELECT order_id, service_type, total, created_at FROM orders ORDER BY created_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String line = "Order #" + rs.getInt("order_id") +
                        " | " + rs.getString("service_type") +
                        " | RM " + String.format("%.2f", rs.getDouble("total")) +
                        " | " + rs.getTimestamp("created_at");
                history.add(line);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
// history

}