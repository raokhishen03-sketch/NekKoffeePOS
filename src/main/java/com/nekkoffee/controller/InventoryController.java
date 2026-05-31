package com.nekkoffee.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import com.nekkoffee.model.InventoryItem;
import com.nekkoffee.util.DatabaseConnection;
import java.sql.*;
import java.util.Optional;

public class InventoryController {

    @FXML private TextField searchField;
    @FXML private TableView<InventoryItem> inventoryTable;
    @FXML private TableColumn<InventoryItem, Integer> colId;
    @FXML private TableColumn<InventoryItem, String> colName;
    @FXML private TableColumn<InventoryItem, Double> colStock;
    @FXML private TableColumn<InventoryItem, String> colUnit;
    @FXML private TableColumn<InventoryItem, Double> colMinLevel;
    @FXML private TableColumn<InventoryItem, String> colStatus;

    private ObservableList<InventoryItem> inventoryList = FXCollections.observableArrayList();
    private Connection conn;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("ingredientID"));
        colName.setCellValueFactory(new PropertyValueFactory<>("ingredientName"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colMinLevel.setCellValueFactory(new PropertyValueFactory<>("minRequiredStock"));
        colStatus.setCellValueFactory(cellData -> {
            InventoryItem item = cellData.getValue();
            String status = item.isLowStock() ? "⚠️ LOW STOCK!" : "✅ OK";
            return new javafx.beans.property.SimpleStringProperty(status);
        });

        try {
            conn = DatabaseConnection.getConnection();
            loadInventoryFromDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Cannot connect to database.");
        }

        inventoryTable.setItems(inventoryList);

        searchField.textProperty().addListener((obs, old, newVal) -> {
            filterInventory(newVal);
        });
    }

    private void loadInventoryFromDatabase() {
        inventoryList.clear();
        try {
            String query = "SELECT * FROM inventory ORDER BY ingredientName";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                InventoryItem item = new InventoryItem(
                        rs.getInt("ingredientID"),
                        rs.getString("ingredientName"),
                        rs.getDouble("stockQuantity"),
                        rs.getString("unit"),
                        rs.getDouble("minRequiredStock")
                );
                inventoryList.add(item);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterInventory(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            inventoryTable.setItems(inventoryList);
        } else {
            ObservableList<InventoryItem> filtered = FXCollections.observableArrayList();
            for (InventoryItem item : inventoryList) {
                if (item.getIngredientName().toLowerCase().contains(keyword.toLowerCase())) {
                    filtered.add(item);
                }
            }
            inventoryTable.setItems(filtered);
        }
    }

    @FXML
    private void handleAdd() {
        Dialog<InventoryItem> dialog = new Dialog<>();
        dialog.setTitle("Add New Ingredient");
        dialog.setHeaderText("Enter ingredient details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Ingredient Name (e.g., Coffee Beans)");

        TextField stockField = new TextField();
        stockField.setPromptText("Stock Quantity");

        TextField unitField = new TextField();
        unitField.setPromptText("Unit (grams, ml, pieces)");

        TextField minLevelField = new TextField();
        minLevelField.setPromptText("Minimum Required Stock");

        form.getChildren().addAll(
                new Label("Ingredient Name:"), nameField,
                new Label("Stock Quantity:"), stockField,
                new Label("Unit:"), unitField,
                new Label("Minimum Required Stock:"), minLevelField
        );

        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText();
                String stockText = stockField.getText();
                String unit = unitField.getText();
                String minText = minLevelField.getText();

                if (name.isEmpty()) {
                    showAlert("Error", "Ingredient name cannot be empty!");
                    return null;
                }

                try {
                    double stock = stockText.isEmpty() ? 0 : Double.parseDouble(stockText);
                    double minLevel = minText.isEmpty() ? 0 : Double.parseDouble(minText);
                    if (unit.isEmpty()) unit = "unit";

                    if (conn != null) {
                        String query = "INSERT INTO inventory (ingredientName, stockQuantity, unit, minRequiredStock) VALUES (?, ?, ?, ?)";
                        PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                        pstmt.setString(1, name);
                        pstmt.setDouble(2, stock);
                        pstmt.setString(3, unit);
                        pstmt.setDouble(4, minLevel);
                        pstmt.executeUpdate();

                        ResultSet rs = pstmt.getGeneratedKeys();
                        int newId = rs.next() ? rs.getInt(1) : 0;
                        rs.close();
                        pstmt.close();

                        return new InventoryItem(newId, name, stock, unit, minLevel);
                    }
                } catch (NumberFormatException e) {
                    showAlert("Error", "Stock and Minimum Level must be numbers!");
                    return null;
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Database Error", "Failed to save ingredient!");
                    return null;
                }
            }
            return null;
        });

        Optional<InventoryItem> result = dialog.showAndWait();
        result.ifPresent(item -> {
            inventoryList.add(item);
            refreshTable();
            showAlert("Success", item.getIngredientName() + " has been added!");
        });
    }

    @FXML
    private void handleEdit() {
        InventoryItem selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select an ingredient to edit.");
            return;
        }

        Dialog<InventoryItem> dialog = new Dialog<>();
        dialog.setTitle("Edit Ingredient");
        dialog.setHeaderText("Edit: " + selected.getIngredientName());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setText(selected.getIngredientName());

        TextField stockField = new TextField();
        stockField.setText(String.valueOf(selected.getStockQuantity()));

        TextField unitField = new TextField();
        unitField.setText(selected.getUnit());

        TextField minLevelField = new TextField();
        minLevelField.setText(String.valueOf(selected.getMinRequiredStock()));

        form.getChildren().addAll(
                new Label("Ingredient Name:"), nameField,
                new Label("Stock Quantity:"), stockField,
                new Label("Unit:"), unitField,
                new Label("Minimum Required Stock:"), minLevelField
        );

        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String name = nameField.getText();
                    double stock = Double.parseDouble(stockField.getText());
                    String unit = unitField.getText();
                    double minLevel = Double.parseDouble(minLevelField.getText());

                    if (conn != null) {
                        String query = "UPDATE inventory SET ingredientName = ?, stockQuantity = ?, unit = ?, minRequiredStock = ? WHERE ingredientID = ?";
                        PreparedStatement pstmt = conn.prepareStatement(query);
                        pstmt.setString(1, name);
                        pstmt.setDouble(2, stock);
                        pstmt.setString(3, unit);
                        pstmt.setDouble(4, minLevel);
                        pstmt.setInt(5, selected.getIngredientID());
                        pstmt.executeUpdate();
                        pstmt.close();
                    }

                    return new InventoryItem(selected.getIngredientID(), name, stock, unit, minLevel);

                } catch (NumberFormatException e) {
                    showAlert("Error", "Stock and Minimum Level must be numbers!");
                    return null;
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Database Error", "Failed to update ingredient!");
                    return null;
                }
            }
            return null;
        });

        Optional<InventoryItem> result = dialog.showAndWait();
        result.ifPresent(item -> {
            selected.setIngredientName(item.getIngredientName());
            selected.setStockQuantity(item.getStockQuantity());
            selected.setUnit(item.getUnit());
            selected.setMinRequiredStock(item.getMinRequiredStock());
            refreshTable();
            showAlert("Success", item.getIngredientName() + " has been updated!");
        });
    }

    @FXML
    private void handleDelete() {
        InventoryItem selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select an ingredient to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete " + selected.getIngredientName() + "?");
        confirm.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (conn != null) {
                    String query = "DELETE FROM inventory WHERE ingredientID = ?";
                    PreparedStatement pstmt = conn.prepareStatement(query);
                    pstmt.setInt(1, selected.getIngredientID());
                    pstmt.executeUpdate();
                    pstmt.close();
                }
                inventoryList.remove(selected);
                refreshTable();
                showAlert("Deleted", selected.getIngredientName() + " has been deleted.");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Database Error", "Failed to delete ingredient!");
            }
        }
    }

    @FXML
    private void handleRestock() {
        InventoryItem selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select an ingredient to restock.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Restock Ingredient");
        dialog.setHeaderText("Restock: " + selected.getIngredientName());
        dialog.setContentText("Enter quantity to add:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(quantityStr -> {
            try {
                double quantity = Double.parseDouble(quantityStr);
                double newStock = selected.getStockQuantity() + quantity;

                if (conn != null) {
                    String query = "UPDATE inventory SET stockQuantity = ? WHERE ingredientID = ?";
                    PreparedStatement pstmt = conn.prepareStatement(query);
                    pstmt.setDouble(1, newStock);
                    pstmt.setInt(2, selected.getIngredientID());
                    pstmt.executeUpdate();
                    pstmt.close();
                }

                selected.setStockQuantity(newStock);
                refreshTable();
                showAlert("Success", "Added " + quantity + " " + selected.getUnit() + " to " + selected.getIngredientName());

                if (selected.isLowStock()) {
                    showAlert("Warning", selected.getIngredientName() + " is still below minimum level!", Alert.AlertType.WARNING);
                }

            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid quantity!");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Database Error", "Failed to restock!");
            }
        });
    }

    @FXML
    private void handleLowStockReport() {
        ObservableList<InventoryItem> lowStockItems = FXCollections.observableArrayList();
        for (InventoryItem item : inventoryList) {
            if (item.isLowStock()) {
                lowStockItems.add(item);
            }
        }

        if (lowStockItems.isEmpty()) {
            showAlert("Low Stock Report", "All ingredients are at adequate levels! ✅");
            return;
        }

        StringBuilder report = new StringBuilder("⚠️ LOW STOCK ITEMS:\n\n");
        for (InventoryItem item : lowStockItems) {
            report.append("• ").append(item.getIngredientName())
                    .append(": ").append(item.getStockQuantity())
                    .append(" ").append(item.getUnit())
                    .append(" (Min: ").append(item.getMinRequiredStock()).append(")\n");
        }

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Low Stock Report");
        alert.setHeaderText("Ingredients Below Minimum Level");
        alert.setContentText(report.toString());
        alert.showAndWait();
    }

    @FXML
    private void handleViewRecipes() {
        StringBuilder report = new StringBuilder("📋 PRODUCT RECIPES:\n\n");
        try {
            String query = "SELECT p.productName, i.ingredientName, pi.quantityUsed, i.unit " +
                    "FROM product_ingredients pi " +
                    "JOIN product p ON pi.productID = p.productID " +
                    "JOIN inventory i ON pi.ingredientID = i.ingredientID " +
                    "ORDER BY p.productName";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            String currentProduct = "";
            while (rs.next()) {
                String productName = rs.getString("productName");
                if (!productName.equals(currentProduct)) {
                    report.append("\n▶ ").append(productName).append(":\n");
                    currentProduct = productName;
                }
                report.append("   - ").append(rs.getString("ingredientName"))
                        .append(": ").append(rs.getDouble("quantityUsed"))
                        .append(" ").append(rs.getString("unit")).append("\n");
            }
            rs.close();
            stmt.close();

            if (currentProduct.isEmpty()) {
                report.append("\nNo recipes found. Add recipes to product_ingredients table.\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            report.append("\nError loading recipes!");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Product Recipes");
        alert.setHeaderText("Ingredients used in each product");
        alert.setContentText(report.toString());
        alert.setResizable(true);
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/nekkoffee/view/main_menu.fxml"));
            Stage stage = (Stage) inventoryTable.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load main menu");
        }
    }

    private void refreshTable() {
        filterInventory(searchField.getText());
        inventoryTable.refresh();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}