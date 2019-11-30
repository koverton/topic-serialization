package com.solace.dev;

public class Foo {
    private String itemName;
    private double price;
    private int quantity;
    private int id;

    public Foo(int id, String itemName, double price, int quantity) {
        this.id = id;
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
    public int getId() {
        return id;
    }
    public String getItemName() {
        return itemName;
    }
    public double getPrice() {
        return price;
    }
    public int getQuantity() {
        return quantity;
    }
}
