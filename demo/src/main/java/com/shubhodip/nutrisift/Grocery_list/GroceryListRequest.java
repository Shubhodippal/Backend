package com.shubhodip.nutrisift.Grocery_list;

import java.util.List;

public class GroceryListRequest {
    private List<GroceryItem> items;

    public GroceryListRequest() {
    }

    public List<GroceryItem> getItems() {
        return items;
    }

    public void setItems(List<GroceryItem> items) {
        this.items = items;
    }
}