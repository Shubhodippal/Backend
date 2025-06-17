package com.shubhodip.nutrisift.recipe;

import java.util.List;

public interface SavedRecipeDAO {
    int saveRecipe(SavedRecipe recipe);
    SavedRecipe getRecipeById(int id);
    List<SavedRecipe> getRecipesByUserId(String uid);
    boolean deleteRecipe(int id);
    List<String> findRecipesByIngredients(String ingredients);
    String saveRecipelogs(String uid, String mail, String prompt, String ingredients, String result);
    
    // Add this new method for recipe discovery
    List<SavedRecipe> discoverRecipes(String calorieRange, String diet, String origin, String course, String cuisine);
}