
package com.nekkoffee.controller;

import com.nekkoffee.util.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class SalesHistoryController {
    @FXML private ListView<String> historyList;

    @FXML
    public void initialize() {
        historyList.getItems().setAll(DatabaseConnection.getSalesHistory());
    }

}
