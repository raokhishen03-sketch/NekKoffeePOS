package com.nekkoffee.model;

public class InventoryItem {
    private int ingredientID;
    private String ingredientName;
    private double stockQuantity;
    private String unit;
    private double minRequiredStock;

    public InventoryItem(int ingredientID, String ingredientName, double stockQuantity, String unit, double minRequiredStock) {
        this.ingredientID = ingredientID;
        this.ingredientName = ingredientName;
        this.stockQuantity = stockQuantity;
        this.unit = unit;
        this.minRequiredStock = minRequiredStock;
    }

    public int getIngredientID() { return ingredientID; }
    public String getIngredientName() { return ingredientName; }
    public double getStockQuantity() { return stockQuantity; }
    public String getUnit() { return unit; }
    public double getMinRequiredStock() { return minRequiredStock; }
    public boolean isLowStock() { return stockQuantity <= minRequiredStock; }

    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }
    public void setStockQuantity(double stockQuantity) { this.stockQuantity = stockQuantity; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setMinRequiredStock(double minRequiredStock) { this.minRequiredStock = minRequiredStock; }
}