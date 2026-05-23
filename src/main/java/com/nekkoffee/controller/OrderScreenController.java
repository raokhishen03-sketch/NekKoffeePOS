package com.nekkoffee.controller;

import com.nekkoffee.model.Product;
import com.nekkoffee.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;

public class OrderScreenController {

    @FXML private VBox menuContainer;
    @FXML private ListView<String> cartListView;

    // Tracking current financial totals running in checkout sidebar (Frontend Dev B's Area)
    private double currentSubtotal = 0.0;
    @FXML private Label lblSubtotal;
    @FXML private Label lblTax;
    @FXML private Label lblTotal;

    @FXML
    public void initialize() {
        loadMenuCategory("All Items");
    }

    /**
     * Category filter logic managed by Dev A.
     * Pulls list sets safely from the database architecture layer.
     */
    private void loadMenuCategory(String category) {
        menuContainer.getChildren().clear();

        // Frontend Dev pulls from our proxy architecture safely:
        List<Product> visibleProducts = DatabaseConnection.getProductsByCategory(category);

        for (Product product : visibleProducts) {
            createDynamicProductCard(product);
        }
    }

    private void createDynamicProductCard(Product product) {
        VBox productWrapper = new VBox();
        productWrapper.setStyle("-fx-background-color: #2a2a35; -fx-background-radius: 8; -fx-padding: 15;");

        HBox itemHeader = new HBox();
        Label nameLabel = new Label(product.getProductName() + " - RM " + String.format("%.2f", product.getPrice()));
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnExpand = new Button("Customize");
        btnExpand.setStyle("-fx-background-color: #e0a96d; -fx-text-fill: #111115; -fx-font-weight: bold; -fx-cursor: hand;");

        itemHeader.getChildren().addAll(nameLabel, spacer, btnExpand);
        productWrapper.getChildren().add(itemHeader);

        // Inline Foodpanda-style sub-tray
        VBox addonContainer = new VBox(8);
        addonContainer.setPadding(new Insets(10, 0, 5, 15));
        addonContainer.setManaged(false);
        addonContainer.setVisible(false);

        Label customizeHeading = new Label("Select Customization Modifications:");
        customizeHeading.setStyle("-fx-text-fill: #8c8c8c; -fx-font-size: 12px;");
        addonContainer.getChildren().add(customizeHeading);

        // Populate modifiers dynamically according to product structural parameters
        ToggleGroup addonGroup = new ToggleGroup();
        List<Product> functionalAddons = DatabaseConnection.getAddonsForProduct(product.getProductID());
        for (Product addon : functionalAddons) {
            RadioButton rb = new RadioButton(addon.getProductName());
            rb.setUserData(addon); // Stashing actual data object securely inside the radio button instance
            rb.setStyle("-fx-text-fill: #d3d3d3;");
            rb.setToggleGroup(addonGroup);
            addonContainer.getChildren().add(rb);
        }

        Button btnAddToCart = new Button("Add Item to Cart");
        btnAddToCart.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        addonContainer.getChildren().add(btnAddToCart);

        productWrapper.getChildren().add(addonContainer);
        menuContainer.getChildren().add(productWrapper);

        btnExpand.setOnAction(e -> {
            boolean visible = !addonContainer.isVisible();
            addonContainer.setVisible(visible);
            addonContainer.setManaged(visible);
            btnExpand.setText(visible ? "Collapse" : "Customize");
        });

        // Cart Insertion logic with pricing computations (Dev B workflow pipeline hook)
        btnAddToCart.setOnAction(e -> {
            RadioButton selectedRb = (RadioButton) addonGroup.getSelectedToggle();
            double finalPrice = product.getPrice();
            String itemLineDescription = product.getProductName();

            if (selectedRb != null) {
                Product selectedAddon = (Product) selectedRb.getUserData();
                finalPrice += selectedAddon.getPrice();
                itemLineDescription += " + " + selectedAddon.getProductName();
            }

            cartListView.getItems().add(itemLineDescription + " | RM " + String.format("%.2f", finalPrice));

            // Increment financials running tallies
            currentSubtotal += finalPrice;
            double tax = currentSubtotal * 0.06;
            double total = currentSubtotal + tax;

            lblSubtotal.setText("RM " + String.format("%.2f", currentSubtotal));
            lblTax.setText("RM " + String.format("%.2f", tax));
            lblTotal.setText("RM " + String.format("%.2f", total));

            // Clean up state
            addonContainer.setVisible(false);
            addonContainer.setManaged(false);
            btnExpand.setText("Customize");
            if(selectedRb != null) selectedRb.setSelected(false);
        });
    }
}
