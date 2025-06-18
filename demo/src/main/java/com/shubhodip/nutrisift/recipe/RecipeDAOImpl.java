package com.shubhodip.nutrisift.recipe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class RecipeDAOImpl implements RecipeDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private ObjectMapper objectMapper; // Add ObjectMapper for JSON processing

    @Override
    public int saveRecipe(SavedRecipe recipe) {
        try {
            // Validate required fields
            System.out.println("Saving recipe: " + recipe.getUid()+recipe.getMail()+recipe.getRecipeName());
            if (recipe.getUid() == null || recipe.getMail() == null) {
                System.err.println("Error: Required fields missing. uid, mail and recipe_name cannot be null");
                return 0;
            }
            
            // Trim and sanitize text fields to prevent SQL issues
            String sanitizedIngredients = recipe.getIngredients() != null ? recipe.getIngredients().replace("'", "''") : null;
            String sanitizedSteps = recipe.getSteps() != null ? recipe.getSteps().replace("'", "''") : null;
            String sanitizedPrompt = recipe.getPrompt() != null ? recipe.getPrompt().replace("'", "''") : null;
            
            // Use the same column order as in your database
            String sql = "INSERT INTO saved_recipe (uid, mail, prompt, recipe_name, ingredients, steps, " +
                         "calories, diet, origin, course, cuisine) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            return jdbcTemplate.update(
                sql, 
                recipe.getUid(), 
                recipe.getMail(), 
                sanitizedPrompt, 
                recipe.getRecipeName(),
                sanitizedIngredients, 
                sanitizedSteps, 
                recipe.getCalories(), 
                recipe.getDiet(), 
                recipe.getOrigin(), 
                recipe.getCourse(), 
                recipe.getCuisine()
            );
        } catch (Exception e) {
            System.err.println("Error in saveRecipe: " + e.getMessage());
            e.printStackTrace();
            
            // Try to provide more detailed error info
            System.err.println(e.getClass().getName() + ": " + e.getMessage()   
            + " | SQL State: " + (e instanceof SQLException ? ((SQLException)e).getSQLState() : "N/A"));
            System.err.println("Name: "+recipe.getRecipeName());
            if (e.getMessage().contains("Data too long")) {
                System.err.println("Data too long for column. Check field length limits.");
            } else if (e.getMessage().contains("cannot be null")) {
                System.err.println("Required field is null. Check uid, mail, and recipe_name.");
            }
            
            return 0;
        }
    }

    @Override
    public SavedRecipe getRecipeById(long id) {
        try {
            String sql = "SELECT * FROM saved_recipe WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, new SavedRecipeRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null; // Recipe not found
        }
    }

    @Override
    public List<SavedRecipe> getRecipesByUserId(String uid) {
        String sql = "SELECT * FROM saved_recipe WHERE uid = ? ORDER BY saved_time_date DESC";
        return jdbcTemplate.query(sql, new SavedRecipeRowMapper(), uid);
    }

    @Override
    public boolean deleteRecipe(long id) {
        String sql = "DELETE FROM saved_recipe WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        return rowsAffected > 0;
    }

    @Override
    public List<SavedRecipe> discoverRecipes(String calorieRange, String diet, String origin, String course, String cuisine) {
        StringBuilder sql = new StringBuilder("SELECT * FROM saved_recipe WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        // Add calorie range filter
        if (calorieRange != null && !calorieRange.equals("any")) {
            switch (calorieRange) {
                case "under300":
                    sql.append(" AND CAST(REGEXP_REPLACE(calories, '[^0-9]', '') AS UNSIGNED) < 300");
                    break;
                case "300-500":
                    sql.append(" AND CAST(REGEXP_REPLACE(calories, '[^0-9]', '') AS UNSIGNED) BETWEEN 300 AND 500");
                    break;
                case "500-800":
                    sql.append(" AND CAST(REGEXP_REPLACE(calories, '[^0-9]', '') AS UNSIGNED) BETWEEN 500 AND 800");
                    break;
                case "over800":
                    sql.append(" AND CAST(REGEXP_REPLACE(calories, '[^0-9]', '') AS UNSIGNED) > 800");
                    break;
            }
        }
        
        // Add diet filter
        if (diet != null && !diet.equals("any")) {
            sql.append(" AND LOWER(diet) LIKE ?");
            params.add("%" + diet.toLowerCase() + "%");
        }
        
        // Add origin filter
        if (origin != null && !origin.equals("any")) {
            sql.append(" AND LOWER(origin) LIKE ?");
            params.add("%" + origin.toLowerCase() + "%");
        }
        
        // Add course filter
        if (course != null && !course.equals("any")) {
            sql.append(" AND LOWER(course) LIKE ?");
            params.add("%" + course.toLowerCase() + "%");
        }
        
        // Add cuisine filter
        if (cuisine != null && !cuisine.equals("any")) {
            sql.append(" AND LOWER(cuisine) LIKE ?");
            params.add("%" + cuisine.toLowerCase() + "%");
        }
        
        // Add order by for consistent results
        sql.append(" ORDER BY id DESC LIMIT 50"); // Limit to 50 recipes for performance
        
        try {
            return jdbcTemplate.query(sql.toString(), params.toArray(), new SavedRecipeRowMapper());
        } catch (Exception e) {
            System.err.println("Error discovering recipes: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public String saveRecipelogs(String uid, String mail, String prompt, String ingredients, String result) {
        try {
            // Validate required fields
            if (uid == null || mail == null) {
                //System.err.println("Error: Required fields missing. uid and mail cannot be null");
                return "Error: Required fields missing. uid and mail cannot be null";
            }
            
            // Sanitize text inputs to prevent SQL issues
            String sanitizedPrompt = prompt != null ? prompt.replace("'", "''") : null;
            String sanitizedIngredients = ingredients != null ? ingredients.replace("'", "''") : null;
            String sanitizedResult = result != null ? result.replace("'", "''") : null;
            
            String sql = "INSERT INTO recipe_logs (uid, mail, prompt, ingredient, result) VALUES (?, ?, ?, ?, ?)";
            
            jdbcTemplate.update(
                sql,
                uid,
                mail,
                sanitizedPrompt,
                sanitizedIngredients,
                sanitizedResult
            );
            
            return "Recipe log saved successfully";
            
        } catch (Exception e) {
            //System.err.println("Error in saveRecipelogs: " + e.getMessage());
            e.printStackTrace();
            return "Error saving recipe log: " + e.getMessage();
        }
    }

    @Override
    public List<String> findRecipesByIngredients(String ingredients) {
        List<String> matchingRecipes = new ArrayList<>();
        Map<String, Double> recipeScores = new HashMap<>();
        
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            // Normalize and split the requested ingredients
            String normalizedRequestIngredients = ingredients.toLowerCase().trim();
            List<String> requestedIngredientsList = java.util.Arrays.asList(
                normalizedRequestIngredients.split("[,\\s]+"));
            
            // Filter out empty strings
            requestedIngredientsList = requestedIngredientsList.stream()
                .filter(ing -> !ing.isEmpty())
                .collect(Collectors.toList());
            
            // Query to get all recipes
            String sql = "SELECT ingredient, result FROM recipe_logs";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    String storedIngredients = rs.getString("ingredient");
                    String recipeJson = rs.getString("result");
                    
                    // Calculate match score with improved algorithm
                    double matchScore = calculateBidirectionalMatchScore(
                        storedIngredients, 
                        requestedIngredientsList
                    );
                    
                    // Only consider recipes with at least 80% match
                    if (matchScore >= 0.8) {
                        recipeScores.put(recipeJson, matchScore);
                    }
                }
            }
            
            // Sort recipes by match score (highest first)
            matchingRecipes = recipeScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return matchingRecipes;
    }

    // Improved bidirectional matching algorithm
    private double calculateBidirectionalMatchScore(String storedIngredients, List<String> requestedIngredientsList) {
        if (storedIngredients == null || requestedIngredientsList.isEmpty()) {
            return 0.0;
        }
        
        // Normalize stored ingredients
        String normalizedStored = storedIngredients.toLowerCase().trim();
        List<String> storedIngredientsList = java.util.Arrays.asList(
            normalizedStored.split("[,\\s]+"));
        
        // Filter out empty strings
        storedIngredientsList = storedIngredientsList.stream()
            .filter(ing -> !ing.isEmpty())
            .collect(Collectors.toList());
        
        if (storedIngredientsList.isEmpty()) {
            return 0.0;
        }
        
        // Count matching ingredients in both directions
        int requestedIngredientsFound = 0;
        int storedIngredientsMatched = 0;
        
        // Count how many requested ingredients are found in the recipe
        for (String reqIng : requestedIngredientsList) {
            for (String storedIng : storedIngredientsList) {
                if (storedIng.equals(reqIng) || 
                    storedIng.contains(reqIng) || 
                    reqIng.contains(storedIng)) {
                    requestedIngredientsFound++;
                    break;
                }
            }
        }
        
        // Count how many recipe ingredients match requested ingredients
        for (String storedIng : storedIngredientsList) {
            for (String reqIng : requestedIngredientsList) {
                if (storedIng.equals(reqIng) || 
                    storedIng.contains(reqIng) || 
                    reqIng.contains(storedIng)) {
                    storedIngredientsMatched++;
                    break;
                }
            }
        }
        
        // Calculate both scores
        double requestedMatchRatio = (double) requestedIngredientsFound / requestedIngredientsList.size();
        double storedMatchRatio = (double) storedIngredientsMatched / storedIngredientsList.size();
        
        // Combined score: heavily weight that all recipe ingredients should be requested ones
        // This ensures recipes with many extra ingredients don't match when only a few ingredients are requested
        return (requestedMatchRatio * 0.3) + (storedMatchRatio * 0.7);
    }

    private static class SavedRecipeRowMapper implements RowMapper<SavedRecipe> {
        @Override
        public SavedRecipe mapRow(ResultSet rs, int rowNum) throws SQLException {
            SavedRecipe recipe = new SavedRecipe();
            recipe.setId(rs.getInt("id"));
            recipe.setUid(rs.getString("uid"));
            recipe.setMail(rs.getString("mail"));
            recipe.setPrompt(rs.getString("prompt"));
            recipe.setRecipeName(rs.getString("recipe_name"));
            recipe.setIngredients(rs.getString("ingredients"));
            recipe.setSteps(rs.getString("steps"));
            recipe.setCalories(rs.getString("calories"));
            recipe.setDiet(rs.getString("diet"));
            recipe.setOrigin(rs.getString("origin"));
            recipe.setCourse(rs.getString("course"));
            recipe.setCuisine(rs.getString("cuisine"));
            recipe.setSavedTimeDate(rs.getTimestamp("saved_time_date").toLocalDateTime());
            return recipe;
        }
    }

        // Add to SavedRecipeDAO class
    public UserProfile getUserProfile(String uid, String mail) {
        UserProfile profile = new UserProfile();
        
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            String sql = "SELECT * FROM profile WHERE uid = ? AND mail = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uid);
                stmt.setString(2, mail);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        profile.setUid(rs.getString("uid"));
                        profile.setEmail(rs.getString("mail")); // Changed from "email" to "mail"
                        profile.setDietaryPreference(rs.getString("diet_pref")); // Changed from "dietary_preference"
                        profile.setAllergies(rs.getString("allergies"));
                        
                        // Set calorie goal based on body goal and BMI
                        profile.setCalorieGoal(calculateCalorieGoal(
                            rs.getDouble("weight"),
                            rs.getDouble("height"),
                            rs.getString("gender"),
                            rs.getString("body_goal")
                        ));
                        
                        profile.setHealthGoals(rs.getString("body_goal")); // Changed from "health_goals"
                        
                        // Set cuisine preferences (this field doesn't exist in your DB)
                        profile.setCuisinePreferences("Not specified");
                        profile.setCity(rs.getString("city"));
                        profile.setCountry(rs.getString("country"));
                    
                        return profile;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }

    // Helper method to calculate calorie goal based on user stats
    private int calculateCalorieGoal(double weight, double height, String gender, String bodyGoal) {
        // Basic BMR calculation (Mifflin-St Jeor Equation)
        double bmr;
        if ("Male".equalsIgnoreCase(gender)) {
            bmr = 10 * weight + 6.25 * height - 5 * 30 + 5; // Age assumed 30 if not available
        } else {
            bmr = 10 * weight + 6.25 * height - 5 * 30 - 161; // Age assumed 30 if not available
        }
        
        // Adjust based on body goal
        if ("Weight loss".equalsIgnoreCase(bodyGoal)) {
            return (int)(bmr * 0.8); // 20% deficit for weight loss
        } else if ("Muscle gain".equalsIgnoreCase(bodyGoal)) {
            return (int)(bmr * 1.2); // 20% surplus for muscle gain
        } else {
            return (int)bmr; // Maintenance
        }
    }

    public void saveMealPlan(String uid, String mail, String mealPlanJson) {
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            String sql = "INSERT INTO meal_plans (uid, email, meal_plan, created_at) VALUES (?, ?, ?, NOW())";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uid);
                stmt.setString(2, mail);
                stmt.setString(3, mealPlanJson);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Map<String, Object>> getUserMealPlans(String uid, String mail) {
        List<Map<String, Object>> mealPlans = new ArrayList<>();
        
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            String sql = "SELECT id, uid, email, meal_plan, created_at FROM meal_plans WHERE uid = ? AND email = ? ORDER BY created_at DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uid);
                stmt.setString(2, mail);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> mealPlanEntry = new HashMap<>();
                        mealPlanEntry.put("id", rs.getInt("id"));
                        mealPlanEntry.put("uid", rs.getString("uid"));
                        mealPlanEntry.put("email", rs.getString("email"));
                        mealPlanEntry.put("created_at", rs.getTimestamp("created_at").toString());
                        
                        // Parse the JSON string back to a Map
                        String mealPlanJson = rs.getString("meal_plan");
                        Map<String, Object> mealPlanData = objectMapper.readValue(mealPlanJson, Map.class);
                        mealPlanEntry.put("meal_plan", mealPlanData);
                        
                        mealPlans.add(mealPlanEntry);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return mealPlans;
    }

    @Override
    public boolean deleteMealPlan(long id) {
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            String sql = "DELETE FROM meal_plans WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}