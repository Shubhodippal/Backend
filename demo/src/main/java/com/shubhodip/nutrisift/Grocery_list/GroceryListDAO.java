package com.shubhodip.nutrisift.Grocery_list;

import java.util.List;

public interface GroceryListDAO {
    List<GroceryItem> getGroceryList(String userId);
    boolean saveGroceryList(String userId, String email, List<GroceryItem> items);
    boolean deleteGroceryList(String userId);
    boolean updateGroceryItem(long itemId, GroceryItem item);
    boolean deleteGroceryItem(long itemId);
    boolean toggleItemCheck(long itemId);
}