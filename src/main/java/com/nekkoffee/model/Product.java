package com.nekkoffee.model;

public class Product {
    private int productID;
    private String productName;
    private double price;
    private String imagePath;
    private boolean isExtension;
    private int categoryID;
    private int stock;  // Untuk inventory management

    // Constructor from database
    public Product(int productID, String productName, double price, String imagePath, boolean isExtension, int categoryID) {
        this.productID = productID;
        this.productName = productName;
        this.price = price;
        this.imagePath = imagePath;
        this.isExtension = isExtension;
        this.categoryID = categoryID;
        this.stock = 0;
    }

    // Full constructor with stock
    public Product(int productID, String productName, double price, String imagePath, boolean isExtension, int categoryID, int stock) {
        this.productID = productID;
        this.productName = productName;
        this.price = price;
        this.imagePath = imagePath;
        this.isExtension = isExtension;
        this.categoryID = categoryID;
        this.stock = stock;
    }

    // Getters
    public int getProductID() { return productID; }
    public String getProductName() { return productName; }
    public double getPrice() { return price; }
    public String getImagePath() { return imagePath; }
    public boolean isExtension() { return isExtension; }
    public int getCategoryID() { return categoryID; }
    public int getStock() { return stock; }

    // Setters
    public void setProductName(String productName) { this.productName = productName; }
    public void setPrice(double price) { this.price = price; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setExtension(boolean isExtension) { this.isExtension = isExtension; }
    public void setCategoryID(int categoryID) { this.categoryID = categoryID; }
    public void setStock(int stock) { this.stock = stock; }
}