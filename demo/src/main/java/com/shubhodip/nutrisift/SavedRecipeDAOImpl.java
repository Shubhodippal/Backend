package com.shubhodip.nutrisift;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SavedRecipeDAOImpl implements SavedRecipeDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public int saveRecipe(SavedRecipe recipe) {
        String sql = "INSERT INTO saved_recipe (uid, mail, prompt, recipe_name, ingredients, steps, calories, diet, origin, course, cuisine) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql, 
                recipe.getUid(), 
                recipe.getMail(), 
                recipe.getPrompt(), 
                recipe.getRecipeName(), 
                recipe.getIngredients(), 
                recipe.getSteps(), 
                recipe.getCalories(), 
                recipe.getDiet(), 
                recipe.getOrigin(), 
                recipe.getCourse(), 
                recipe.getCuisine());
    }

    @Override
    public SavedRecipe getRecipeById(int id) {
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
    public boolean deleteRecipe(int id) {
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
}