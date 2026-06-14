package com.nekkoffee.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.List;

// Simple Order model for table rows
class Order {
    private int orderId;
    private String serviceType;
    private double total;
    private String date;
    private String phoneNumber;

    public Order(int orderId, String serviceType, double total, String date, String phoneNumber) {
        this.orderId = orderId;
        this.serviceType = serviceType;
        this.total = total;
        this.date = date;
        this.phoneNumber = phoneNumber;
    }

    public int getOrderId() { return orderId; }
    public String getServiceType() { return serviceType; }
    public double getTotal() { return total; }
    public String getDate() { return date; }
    public String getPhoneNumber() {
        return phoneNumber;
    }
}

public class SalesHistoryController {

    @FXML private TextField searchField;
    @FXML private DatePicker startDate;
    @FXML private DatePicker endDate;
    @FXML private TableView<Order> historyTable;
    @FXML private TableColumn<Order, Number> colOrderId;
    @FXML private TableColumn<Order, String> colDate;
    @FXML private TableColumn<Order, String> colServiceType;
    @FXML private TableColumn<Order, Number> colTotal;
    @FXML private Label lblTotalSales;
    @FXML private Label lblOrderCount;
    @FXML private TableColumn<Order, String> colPhoneNumber;

    private ObservableList<Order> orders = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Bind table columns
        colOrderId.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getOrderId()));
        colDate.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDate()));
        colServiceType.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getServiceType()));
        colTotal.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getTotal()));
        colPhoneNumber.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getPhoneNumber()
                )
        );


        // Load initial data
        loadSalesHistory();
    }

    private void loadSalesHistory() {

        orders.clear();

        try {
            java.sql.Connection conn =
                    com.nekkoffee.util.DatabaseConnection.getConnection();

            String sql =
                    "SELECT order_id, service_type, total, orderDate, phone_number " +
                            "FROM orders ORDER BY orderDate DESC";

            java.sql.PreparedStatement stmt =
                    conn.prepareStatement(sql);

            java.sql.ResultSet rs =
                    stmt.executeQuery();

            while (rs.next()) {

                orders.add(
                        new Order(
                                rs.getInt("order_id"),
                                rs.getString("service_type"),
                                rs.getDouble("total"),
                                rs.getString("orderDate"),
                                rs.getString("phone_number")
                        )
                );
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        historyTable.setItems(orders);

        updateSummary();
    }

    @FXML
    private void filterHistory() {
        String searchText = searchField.getText();
        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();


        // TODO: Replace with DatabaseConnection.getFilteredSalesHistory(searchText, start, end)
        List<Order> filtered = orders.filtered(order -> {
            boolean matches = true;

            if (searchText != null && !searchText.isEmpty()) {
                matches = String.valueOf(order.getOrderId()).contains(searchText)
                        || order.getServiceType().toLowerCase().contains(searchText.toLowerCase())
                        || (order.getPhoneNumber() != null &&
                        order.getPhoneNumber().contains(searchText));
            }

            if (start != null) {
                matches &= LocalDate.parse(order.getDate().substring(0, 10))
                        .isAfter(start.minusDays(1));
            }

            if (end != null) {
                matches &= LocalDate.parse(order.getDate().substring(0, 10))
                        .isBefore(end.plusDays(1));
            }

            return matches;
        });


        historyTable.setItems(FXCollections.observableArrayList(filtered));
        updateSummary();
    }

    private void updateSummary() {
        double totalSales = historyTable.getItems().stream().mapToDouble(Order::getTotal).sum();
        int orderCount = historyTable.getItems().size();

        lblTotalSales.setText("Total Sales: RM " + String.format("%.2f", totalSales));
        lblOrderCount.setText("Orders: " + orderCount);
    }
}
