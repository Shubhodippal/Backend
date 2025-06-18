package com.shubhodip.nutrisift.Grocery_list;

import java.util.List;

public class GroceryListResponse {
    private List<GroceryItem> items;
    private String message;
    private boolean success;

    public GroceryListResponse() {
    }

    public GroceryListResponse(List<GroceryItem> items, String message, boolean success) {
        this.items = items;
        this.message = message;
        this.success = success;
    }

    public List<GroceryItem> getItems() {
        return items;
    }

    public void setItems(List<GroceryItem> items) {
        this.items = items;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
