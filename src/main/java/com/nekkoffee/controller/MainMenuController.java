package com.nekkoffee.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class MainMenuController {

    @FXML private StackPane contentArea;
    @FXML private Button btnOrder;
    @FXML
    private void handleNavInventory() {
        try {
            // 1. Load the FXML file your friend designed (ensure the path matches your project resource folders)
            Parent inventoryView = FXMLLoader.load(getClass().getResource("/com/nekkoffee/view/inventory_view.fxml"));

            // 2. Clear the main central display area (contentArea StackPane)
            contentArea.getChildren().clear();

            // 3. Inject the new inventory user interface right into the workspace
            contentArea.getChildren().add(inventoryView);

            System.out.println("✅ Navigated to Inventory Management Screen smoothly.");
        } catch (IOException e) {
            System.out.println("❌ Error: Could not locate or load inventory_view.fxml!");
            e.printStackTrace();
        }
    }
    @FXML
    public void initialize() {
        // Code executing automatically on UI load goes here (e.g., checking DB status)
    }

    @FXML
    private void handleNavOrder() {
        try {
            // How your team will switch sub-screens smoothly without opening multiple windows
            Parent orderView = FXMLLoader.load(getClass().getResource("/com/nekkoffee/view/order_screen.fxml"));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(orderView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}