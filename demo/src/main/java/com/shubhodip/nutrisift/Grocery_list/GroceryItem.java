package com.shubhodip.nutrisift.Grocery_list;

import java.sql.Timestamp;

public class GroceryItem {
    private long id;
    private String uid;
    private String mail;
    private String state;
    private String itemName;
    private String quantity;
    private String category;
    private Timestamp creationTime;
    private boolean checked;
    private String[] meals;

    // Default constructor
    public GroceryItem() {
    }

    // Constructor with essential fields
    public GroceryItem(String uid, String mail, String itemName, String quantity, String category) {
        this.uid = uid;
        this.mail = mail;
        this.itemName = itemName;
        this.quantity = quantity;
        this.category = category;
        this.state = "active";
    }

    // Full constructor
    public GroceryItem(long id, String uid, String mail, String state, String itemName, 
                      String quantity, String category, Timestamp creationTime) {
        this.id = id;
        this.uid = uid;
        this.mail = mail;
        this.state = state;
        this.itemName = itemName;
        this.quantity = quantity;
        this.category = category;
        this.creationTime = creationTime;
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getName() {
        return itemName;
    }

    public void setName(String name) {
        this.itemName = name;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public boolean isChecked() {
        return "checked".equals(state);
    }

    public void setChecked(boolean checked) {
        this.state = checked ? "checked" : "active";
    }

    public String[] getMeals() {
        return meals;
    }

    public void setMeals(String[] meals) {
        this.meals = meals;
    }
}