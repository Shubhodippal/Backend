package com.shubhodip.nutrisift;

public class RecipeFilterRequest {
    private String calorieRange;
    private String diet;
    private String origin;
    private String course;
    private String cuisine;
    
    // Default constructor
    public RecipeFilterRequest() {
    }
    
    // Getters and setters
    public String getCalorieRange() {
        return calorieRange;
    }
    
    public void setCalorieRange(String calorieRange) {
        this.calorieRange = calorieRange;
    }
    
    public String getDiet() {
        return diet;
    }
    
    public void setDiet(String diet) {
        this.diet = diet;
    }
    
    public String getOrigin() {
        return origin;
    }
    
    public void setOrigin(String origin) {
        this.origin = origin;
    }
    
    public String getCourse() {
        return course;
    }
    
    public void setCourse(String course) {
        this.course = course;
    }
    
    public String getCuisine() {
        return cuisine;
    }
    
    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }
}