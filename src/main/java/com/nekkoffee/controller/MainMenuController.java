package com.nekkoffee.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import javafx.scene.layout.BorderPane;

public class MainMenuController {

    @FXML private StackPane contentArea;
    @FXML private Button btnOrder;
    @FXML private Button btnInventory;

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

    @FXML
    private void handleNavInventory() {
        try {
            Parent inventoryView = FXMLLoader.load(getClass().getResource("/com/nekkoffee/view/inventory_view.fxml"));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(inventoryView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}