package com.shubhodip.nutrisift;

import java.time.LocalDateTime;

public class SavedRecipe {
    private int id;
    private String uid;
    private String mail;
    private String prompt;
    private String recipeName;
    private String ingredients;
    private String steps;
    private String calories;
    private String diet;
    private String origin;
    private String course;
    private String cuisine;
    private LocalDateTime savedTimeDate;
    
    // Default constructor
    public SavedRecipe() {
    }
    
    // Getters and setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getUid() {
        return uid;
    }
    
    public void setUid(String uid) {
        this.uid = uid;
    }
    
    public String getMail() {
        return mail;
    }
    
    public void setMail(String mail) {
        this.mail = mail;
    }
    
    public String getPrompt() {
        return prompt;
    }
    
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    
    public String getRecipeName() {
        return recipeName;
    }
    
    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }
    
    public String getIngredients() {
        return ingredients;
    }
    
    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }
    
    public String getSteps() {
        return steps;
    }
    
    public void setSteps(String steps) {
        this.steps = steps;
    }
    
    public String getCalories() {
        return calories;
    }
    
    public void setCalories(String calories) {
        this.calories = calories;
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
    
    public LocalDateTime getSavedTimeDate() {
        return savedTimeDate;
    }
    
    public void setSavedTimeDate(LocalDateTime savedTimeDate) {
        this.savedTimeDate = savedTimeDate;
    }
}