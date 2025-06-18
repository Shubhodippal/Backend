package com.shubhodip.nutrisift.recipe;

import java.util.List;
import java.util.Map;

public interface RecipeDAO {
    int saveRecipe(SavedRecipe recipe);
    SavedRecipe getRecipeById(long id);
    List<SavedRecipe> getRecipesByUserId(String uid);
    boolean deleteRecipe(long id);
    List<String> findRecipesByIngredients(String ingredients);
    UserProfile getUserProfile(String uid, String mail);
    void saveMealPlan(String uid, String mail, String mealPlanJson);
    String saveRecipelogs(String uid, String mail, String prompt, String ingredients, String result);
    List<Map<String, Object>> getUserMealPlans(String uid, String mail);
    boolean deleteMealPlan(long id);
    
    // Add this new method for recipe discovery
    List<SavedRecipe> discoverRecipes(String calorieRange, String diet, String origin, String course, String cuisine);
}