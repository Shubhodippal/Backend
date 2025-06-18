package com.shubhodip.nutrisift.recipe;
public class UserProfile {
    private String uid;
    private String email;
    private String dietaryPreference;
    private String allergies;
    private int calorieGoal;
    private String healthGoals;
    private String cuisinePreferences;
    private String city;
    private String country;
    
    // Getters and setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getDietaryPreference() { return dietaryPreference; }
    public void setDietaryPreference(String dietaryPreference) { this.dietaryPreference = dietaryPreference; }
    
    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }
    
    public int getCalorieGoal() { return calorieGoal; }
    public void setCalorieGoal(int calorieGoal) { this.calorieGoal = calorieGoal; }
    
    public String getHealthGoals() { return healthGoals; }
    public void setHealthGoals(String healthGoals) { this.healthGoals = healthGoals; }
    
    public String getCuisinePreferences() { return cuisinePreferences; }
    public void setCuisinePreferences(String cuisinePreferences) { this.cuisinePreferences = cuisinePreferences; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}