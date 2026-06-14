package com.nekkoffee.controller;

import com.nekkoffee.model.Product;
import com.nekkoffee.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


import java.util.ArrayList;
import java.util.List;

public class OrderScreenController {

    @FXML private FlowPane menuContainer;
    @FXML private ListView<String> cartListView;

    // Tracking current financial totals running in checkout sidebar (Frontend Dev B's Area)
    private double currentSubtotal = 0.0;
    @FXML private Label lblSubtotal;
    @FXML private Label lblTax;
    @FXML private Label lblTotal;

    private List<Product> activeCart = new ArrayList<>();

    @FXML private TextField txtPhoneNumber;   // loyalty phone number field
    @FXML private ChoiceBox<String> choiceServiceType; // dine-in/takeaway dropdown
    @FXML private Button btnConfirmOrder;

    @FXML
    public void initialize() {
        // Populate the dropdown
        choiceServiceType.getItems().addAll("Dine-In", "Takeaway");

        // Safe fallback to prevent null crash
        choiceServiceType.setValue("Dine-In");

        loadMenuCategory("All Items");
    }

    /**
     * Category filter logic managed by Dev A.
     * Pulls list sets safely from the database architecture layer.
     */
    private void loadMenuCategory(String category) {

        menuContainer.getChildren().clear();

        List<Product> visibleProducts =
                DatabaseConnection.getProductsByCategory(category);

        System.out.println(
                "Category = " + category +
                        " | Products Found = " +
                        visibleProducts.size()
        );

        for(Product product : visibleProducts){
            System.out.println(product.getProductName());
            createDynamicProductCard(product);
        }
    }

    private void createDynamicProductCard(Product product) {
        VBox productWrapper = new VBox();

        productWrapper.setPrefWidth(220);
        productWrapper.setPrefHeight(240);

        productWrapper.setSpacing(10);

        productWrapper.setStyle(
                "-fx-background-color:#2a2a35;" +
                        "-fx-background-radius:12;" +
                        "-fx-padding:15;"
        );
        productWrapper.setStyle("-fx-background-color: #2a2a35; -fx-background-radius: 8; -fx-padding: 15;");

        ImageView imageView = new ImageView();

        try {
            String imageFile = product.getImagePath();

            Image image = new Image(
                    getClass().getResourceAsStream(
                            "/com/nekkoffee/images/" + imageFile
                    )
            );

            imageView.setImage(image);

        } catch (Exception ex) {
            System.out.println("Image not found: " + product.getImagePath());
        }

        imageView.setFitWidth(180);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        HBox itemHeader = new HBox();
        Label nameLabel = new Label(product.getProductName() + " - RM " + String.format("%.2f", product.getPrice()));
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnExpand = new Button("Customize");
        btnExpand.setStyle("-fx-background-color: #e0a96d; -fx-text-fill: #111115; -fx-font-weight: bold; -fx-cursor: hand;");

        itemHeader.getChildren().addAll(nameLabel, spacer, btnExpand);
        productWrapper.getChildren().addAll(
                imageView,
                itemHeader
        );

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
                activeCart.add(selectedAddon);
            }
            activeCart.add(product);

            cartListView.getItems().add(itemLineDescription + " | RM " + String.format("%.2f", finalPrice));

            // Increment financials running tallies
            currentSubtotal += finalPrice;
            double tax = currentSubtotal * 0.06;
            double total = currentSubtotal + tax;

            lblSubtotal.setText("RM " + String.format("%.2f", currentSubtotal));
            lblTax.setText("RM " + String.format("%.2f", tax));
            lblTotal.setText("RM " + String.format("%.2f", total));

            lblSubtotal.setStyle("-fx-text-fill: white;");
            lblTax.setStyle("-fx-text-fill: white;");
            lblTotal.setStyle("-fx-text-fill: #e0a96d; -fx-font-size: 20px; -fx-font-weight: bold;");

            updateTotals();
            addonContainer.setVisible(false);
            addonContainer.setManaged(false);
            btnExpand.setText("Customize");
            if(selectedRb != null) selectedRb.setSelected(false);
        });
    }
    // --- Sidebar Cart Functions ---

    @FXML private Button btnRemoveItem;
    @FXML private Button btnClearCart;
    @FXML private Button btnCheckout;



    @FXML
    private void showAllItems() {
        loadProducts("All Items");
    }

    @FXML
    private void showCoffee() {
        loadProducts("Coffee");
    }

    @FXML
    private void showNonCoffee() {
        loadProducts("Non-Coffee");
    }

    @FXML
    private void showFood() {
        loadProducts("Food");
    }


    private void loadProducts(String category) {

        menuContainer.getChildren().clear();

        List<Product> products =
                DatabaseConnection.getProductsByCategory(category);

        for(Product product : products) {
            createDynamicProductCard(product);
        }
    }
    @FXML
    private void removeSelectedItem() {
        String selectedItem = cartListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            // Extract price from string "Item | RM xx.xx"
            String[] parts = selectedItem.split("RM");
            if (parts.length > 1) {
                double price = Double.parseDouble(parts[1].trim());
                currentSubtotal -= price;
            }
            cartListView.getItems().remove(selectedItem);
            recalcTotals();
        }
    }

    @FXML
    private void clearCart() {
        cartListView.getItems().clear();
        currentSubtotal = 0.0;
        recalcTotals();
    }

    @FXML
    private void checkoutCart() {
        if (cartListView.getItems().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Your cart is empty!");
            alert.showAndWait();
            return;
        }

        double tax = currentSubtotal * 0.06;
        double total = currentSubtotal + tax;

        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                "Checkout Successful!\nSubtotal: RM " + String.format("%.2f", currentSubtotal) +
                        "\nTax: RM " + String.format("%.2f", tax) +
                        "\nTotal: RM " + String.format("%.2f", total));
        alert.setHeaderText("Order Summary");
        alert.showAndWait();

        // Reset cart after checkout
        clearCart();
    }

    // Utility to recalc labels
    private void recalcTotals() {
        double tax = currentSubtotal * 0.06;
        double total = currentSubtotal + tax;

        lblSubtotal.setText("RM " + String.format("%.2f", currentSubtotal));
        lblTax.setText("RM " + String.format("%.2f", tax));
        lblTotal.setText("RM " + String.format("%.2f", total));
    }

    private double calculateSubtotal() {
        return activeCart.stream().mapToDouble(Product::getPrice).sum();
    }

    private double calculateTax(double subtotal) {
        return subtotal * 0.06;
    }

    private double calculateGrandTotal(double subtotal, double tax) {
        return subtotal + tax;
    }

    private void updateTotals() {
        double subtotal = calculateSubtotal();
        double tax = calculateTax(subtotal);
        double total = calculateGrandTotal(subtotal, tax);

        lblSubtotal.setText("RM " + String.format("%.2f", subtotal));
        lblTax.setText("RM " + String.format("%.2f", tax));
        lblTotal.setText("RM " + String.format("%.2f", total));

        lblSubtotal.setStyle("-fx-text-fill: white;");
        lblTax.setStyle("-fx-text-fill: white;");
        lblTotal.setStyle("-fx-text-fill: #e0a96d; -fx-font-size: 20px; -fx-font-weight: bold;");
    }
    private void applyLoyaltyPoints(double billTotal) {
        String phone = txtPhoneNumber.getText();
        if (phone != null && !phone.isEmpty()) {
            DatabaseConnection.addLoyaltyPoints(phone, billTotal);
        }
    }
    @FXML
    private void confirmOrder() {
        double subtotal = calculateSubtotal();
        double tax = calculateTax(subtotal);
        double total = calculateGrandTotal(subtotal, tax);

        applyLoyaltyPoints(total);

        String serviceType = choiceServiceType.getValue();

        int orderId = DatabaseConnection.saveOrder(
                serviceType,
                subtotal,
                tax,
                total,
                txtPhoneNumber.getText()
        );
        for (Product p : activeCart) {
            DatabaseConnection.saveOrderItem(orderId, p.getProductID(), p.getPrice());
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                "Order Confirmed!\nService: " + serviceType +
                        "\nTotal: RM " + String.format("%.2f", total));
        alert.setHeaderText("Order Success");
        alert.showAndWait();

        activeCart.clear();
        cartListView.getItems().clear();
        updateTotals();
    }
}
