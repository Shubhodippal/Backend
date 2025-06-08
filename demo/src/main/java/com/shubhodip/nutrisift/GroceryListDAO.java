package com.shubhodip.nutrisift;

import java.util.List;

public interface GroceryListDAO {
    List<GroceryItem> getGroceryList(String userId);
    boolean saveGroceryList(String userId, String email, List<GroceryItem> items);
    boolean deleteGroceryList(String userId);
    boolean updateGroceryItem(int itemId, GroceryItem item);
    boolean deleteGroceryItem(int itemId);
    boolean toggleItemCheck(int itemId);
}