package com.shubhodip.nutrisift;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recipes")
public class RecipeController {

    @Autowired
    private SavedRecipeDAO savedRecipeDAO;

    @PostMapping("/save")
    public ResponseEntity<?> saveRecipe(@RequestBody SaveRecipeRequest request) {
        try {
            SavedRecipe recipe = new SavedRecipe();
            recipe.setUid(request.getUid());
            recipe.setMail(request.getMail());
            recipe.setPrompt(request.getPrompt());
            recipe.setRecipeName(request.getRecipeName());
            recipe.setIngredients(request.getIngredients());
            recipe.setSteps(request.getSteps());
            recipe.setCalories(request.getCalories());
            recipe.setDiet(request.getDiet());
            recipe.setOrigin(request.getOrigin());
            recipe.setCourse(request.getCourse());
            recipe.setCuisine(request.getCuisine());
            
            int rows = savedRecipeDAO.saveRecipe(recipe);
            if (rows > 0) {
                return ResponseEntity.ok("Recipe saved successfully");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save recipe");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while saving recipe: " + e.getMessage());
        }
    }

    @GetMapping("/user/{uid}")
    public ResponseEntity<?> getUserRecipes(@PathVariable String uid) {
        try {
            List<SavedRecipe> recipes = savedRecipeDAO.getRecipesByUserId(uid);
            return ResponseEntity.ok(recipes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving recipes: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRecipeById(@PathVariable int id) {
        try {
            SavedRecipe recipe = savedRecipeDAO.getRecipeById(id);
            if (recipe != null) {
                return ResponseEntity.ok(recipe);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Recipe not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while retrieving recipe: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipe(@PathVariable int id) {
        try {
            boolean deleted = savedRecipeDAO.deleteRecipe(id);
            if (deleted) {
                return ResponseEntity.ok("Recipe deleted successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Recipe not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while deleting recipe: " + e.getMessage());
        }
    }

    @PostMapping("/discover")
    public ResponseEntity<?> discoverRecipes(@RequestBody RecipeFilterRequest request) {
        try {
            List<SavedRecipe> recipes = savedRecipeDAO.discoverRecipes(
                request.getCalorieRange(),
                request.getDiet(),
                request.getOrigin(),
                request.getCourse(),
                request.getCuisine()
            );
            
            if (recipes.isEmpty()) {
                // If no recipes found, generate some placeholder recipes
                List<Map<String, Object>> placeholderRecipes = generatePlaceholderRecipes(request);
                return ResponseEntity.ok(placeholderRecipes);
            }
            
            // Convert to format expected by the frontend
            List<Map<String, Object>> formattedRecipes = new ArrayList<>();
            for (SavedRecipe recipe : recipes) {
                Map<String, Object> formattedRecipe = new HashMap<>();
                formattedRecipe.put("id", recipe.getId());
                formattedRecipe.put("title", recipe.getRecipeName());
                formattedRecipe.put("calories", recipe.getCalories());
                formattedRecipe.put("diet", recipe.getDiet());
                formattedRecipe.put("origin", recipe.getOrigin());
                formattedRecipe.put("course", recipe.getCourse());
                formattedRecipe.put("cuisine", recipe.getCuisine());
                formattedRecipe.put("ingredients", recipe.getIngredients());
                formattedRecipe.put("prepTime", 30); // Default value
                formattedRecipe.put("cookTime", 45); // Default value
                formattedRecipe.put("imageUrl", null); // Let frontend handle default image
                
                formattedRecipes.add(formattedRecipe);
            }
            
            return ResponseEntity.ok(formattedRecipes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while discovering recipes: " + e.getMessage());
        }
    }

    // Helper method to generate placeholder recipes when database is empty or no matches
    private List<Map<String, Object>> generatePlaceholderRecipes(RecipeFilterRequest request) {
        List<Map<String, Object>> placeholders = new ArrayList<>();
        
        // Example placeholder recipe data - create 8 recipes
        String[] titles = {
            "Creamy Pasta Primavera", 
            "Spicy Chicken Curry", 
            "Mediterranean Salad", 
            "Classic Beef Burger", 
            "Chocolate Lava Cake", 
            "Vegetable Stir Fry", 
            "Fresh Berry Smoothie",
            "Homemade Pizza Margherita"
        };
        
        String[] diets = {"Vegetarian", "Non-Vegetarian", "Vegan", "Keto", "Paleo", "Low-Carb", "Gluten-Free", "Dairy-Free"};
        String[] origins = {"Italian", "Indian", "Mediterranean", "American", "French", "Chinese", "International", "Mexican"};
        String[] courses = {"Main Course", "Main Course", "Appetizer", "Main Course", "Dessert", "Main Course", "Breakfast", "Main Course"};
        String[] cuisines = {"Traditional", "Traditional", "Fusion", "Comfort Food", "Gourmet", "Healthy", "Modern", "Traditional"};
        String[] calories = {"450", "650", "320", "750", "580", "380", "250", "720"};
        
        String[][] ingredientsList = {
            {"Pasta", "Bell peppers", "Cream", "Parmesan cheese", "Garlic"},
            {"Chicken", "Curry powder", "Coconut milk", "Onions", "Tomatoes"},
            {"Lettuce", "Cucumber", "Feta cheese", "Olives", "Olive oil"},
            {"Ground beef", "Burger buns", "Lettuce", "Tomato", "Cheese"},
            {"Chocolate", "Flour", "Eggs", "Sugar", "Vanilla extract"},
            {"Tofu", "Bell peppers", "Broccoli", "Soy sauce", "Ginger"},
            {"Strawberries", "Banana", "Yogurt", "Honey", "Ice"},
            {"Pizza dough", "Tomato sauce", "Mozzarella", "Basil", "Olive oil"}
        };
        
        // Apply user filters to the placeholders when generating
        for (int i = 0; i < titles.length; i++) {
            // Skip recipes that don't match the diet filter
            if (request.getDiet() != null && !request.getDiet().equals("any") && 
                !diets[i].toLowerCase().contains(request.getDiet().toLowerCase())) {
                continue;
            }
            
            // Skip recipes that don't match the origin filter
            if (request.getOrigin() != null && !request.getOrigin().equals("any") && 
                !origins[i].toLowerCase().contains(request.getOrigin().toLowerCase())) {
                continue;
            }
            
            // Skip recipes that don't match the course filter
            if (request.getCourse() != null && !request.getCourse().equals("any") && 
                !courses[i].toLowerCase().contains(request.getCourse().toLowerCase())) {
                continue;
            }
            
            // Skip recipes that don't match the cuisine filter
            if (request.getCuisine() != null && !request.getCuisine().equals("any") && 
                !cuisines[i].toLowerCase().contains(request.getCuisine().toLowerCase())) {
                continue;
            }
            
            // Skip recipes that don't match the calorie range
            int calorieValue = Integer.parseInt(calories[i]);
            if (request.getCalorieRange() != null && !request.getCalorieRange().equals("any")) {
                switch (request.getCalorieRange()) {
                    case "under300":
                        if (calorieValue >= 300) continue;
                        break;
                    case "300-500":
                        if (calorieValue < 300 || calorieValue > 500) continue;
                        break;
                    case "500-800":
                        if (calorieValue < 500 || calorieValue > 800) continue;
                        break;
                    case "over800":
                        if (calorieValue <= 800) continue;
                        break;
                }
            }
            
            Map<String, Object> recipe = new HashMap<>();
            recipe.put("id", i + 1);
            recipe.put("title", titles[i]);
            recipe.put("calories", calories[i]);
            recipe.put("diet", diets[i]);
            recipe.put("origin", origins[i]);
            recipe.put("course", courses[i]);
            recipe.put("cuisine", cuisines[i]);
            recipe.put("ingredients", String.join(", ", ingredientsList[i]));
            recipe.put("prepTime", 30); // Default
            recipe.put("cookTime", 45); // Default
            recipe.put("imageUrl", null); // Let frontend handle this
            
            placeholders.add(recipe);
        }
        
        return placeholders;
    }
}