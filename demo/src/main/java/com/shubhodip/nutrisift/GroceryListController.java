package com.shubhodip.nutrisift;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/grocerylist")
public class GroceryListController {

    @Autowired
    private GroceryListDAO groceryListDAO;

    @GetMapping("/{userId}")
    public ResponseEntity<GroceryListResponse> getGroceryList(@PathVariable String userId) {
        try {
            List<GroceryItem> items = groceryListDAO.getGroceryList(userId);
            
            GroceryListResponse response = new GroceryListResponse();
            response.setItems(items);
            response.setSuccess(true);
            response.setMessage("Grocery list retrieved successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            GroceryListResponse errorResponse = new GroceryListResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error retrieving grocery list: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{userId}")
    public ResponseEntity<GroceryListResponse> saveGroceryList(
            @PathVariable String userId, 
            @RequestBody GroceryListRequest request) {
        try {
            // Get the user's email from the first item, or use a default
            String email = request.getItems().isEmpty() ? "user@example.com" : 
                          (request.getItems().get(0).getMail() != null ? 
                           request.getItems().get(0).getMail() : "user@example.com");
            
            boolean saved = groceryListDAO.saveGroceryList(userId, email, request.getItems());
            
            GroceryListResponse response = new GroceryListResponse();
            
            if (saved) {
                // Retrieve the latest list from the database to include generated IDs
                List<GroceryItem> updatedItems = groceryListDAO.getGroceryList(userId);
                response.setItems(updatedItems);
                response.setSuccess(true);
                response.setMessage("Grocery list saved successfully");
                return ResponseEntity.ok(response);
            } else {
                response.setSuccess(false);
                response.setMessage("Failed to save grocery list");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            GroceryListResponse errorResponse = new GroceryListResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error saving grocery list: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<GroceryListResponse> deleteGroceryList(@PathVariable String userId) {
        try {
            boolean deleted = groceryListDAO.deleteGroceryList(userId);
            
            GroceryListResponse response = new GroceryListResponse();
            
            if (deleted) {
                response.setSuccess(true);
                response.setMessage("Grocery list deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.setSuccess(false);
                response.setMessage("No grocery list found to delete");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            GroceryListResponse errorResponse = new GroceryListResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error deleting grocery list: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/item/{itemId}")
    public ResponseEntity<GroceryListResponse> updateGroceryItem(
            @PathVariable int itemId,
            @RequestBody GroceryItem item) {
        try {
            boolean updated = groceryListDAO.updateGroceryItem(itemId, item);
            
            GroceryListResponse response = new GroceryListResponse();
            
            if (updated) {
                response.setSuccess(true);
                response.setMessage("Item updated successfully");
                // Get updated list for the user
                List<GroceryItem> updatedItems = groceryListDAO.getGroceryList(item.getUid());
                response.setItems(updatedItems);
                return ResponseEntity.ok(response);
            } else {
                response.setSuccess(false);
                response.setMessage("Failed to update item");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            GroceryListResponse errorResponse = new GroceryListResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error updating item: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<GroceryListResponse> deleteGroceryItem(
            @PathVariable int itemId,
            @RequestBody GroceryItem item) {  // Need the item to get the userId for the updated list
        try {
            boolean deleted = groceryListDAO.deleteGroceryItem(itemId);
            
            GroceryListResponse response = new GroceryListResponse();
            
            if (deleted) {
                response.setSuccess(true);
                response.setMessage("Item deleted successfully");
                // Get updated list for the user
                List<GroceryItem> updatedItems = groceryListDAO.getGroceryList(item.getUid());
                response.setItems(updatedItems);
                return ResponseEntity.ok(response);
            } else {
                response.setSuccess(false);
                response.setMessage("Failed to delete item");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            GroceryListResponse errorResponse = new GroceryListResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error deleting item: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PutMapping("/item/{itemId}/toggle")
    public ResponseEntity<GroceryListResponse> toggleItemCheck(
            @PathVariable int itemId,
            @RequestBody GroceryItem item) {  // Need the item to get the userId for the updated list
        try {
            boolean toggled = groceryListDAO.toggleItemCheck(itemId);
            
            GroceryListResponse response = new GroceryListResponse();
            
            if (toggled) {
                response.setSuccess(true);
                response.setMessage("Item check status toggled successfully");
                // Get updated list for the user
                List<GroceryItem> updatedItems = groceryListDAO.getGroceryList(item.getUid());
                response.setItems(updatedItems);
                return ResponseEntity.ok(response);
            } else {
                response.setSuccess(false);
                response.setMessage("Failed to toggle item check status");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            GroceryListResponse errorResponse = new GroceryListResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error toggling item check status: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
