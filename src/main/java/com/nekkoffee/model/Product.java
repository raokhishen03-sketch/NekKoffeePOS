package com.nekkoffee.model;

public class Product {
    private int productID;
    private String productName;
    private double price;
    private String imagePath;
    private boolean isExtension; // Matches the tinyint(1) column in SQL
    private int categoryID;

    public Product(int productID, String productName, double price, String imagePath, boolean isExtension, int categoryID) {
        this.productID = productID;
        this.productName = productName;
        this.price = price;
        this.imagePath = imagePath;
        this.isExtension = isExtension;
        this.categoryID = categoryID;
    }

    // Getters
    public int getProductID() { return productID; }
    public String getProductName() { return productName; }
    public double getPrice() { return price; }
    public String getImagePath() { return imagePath; }
    public boolean isExtension() { return isExtension; }
    public int getCategoryID() { return categoryID; }
}