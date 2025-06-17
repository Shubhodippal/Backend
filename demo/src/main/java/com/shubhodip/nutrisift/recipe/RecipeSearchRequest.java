package com.shubhodip.nutrisift.recipe;

public class RecipeSearchRequest {
    private String uid;
    private String mail;
    private String query;
    private String diet;
    private String cuisine;
    private String course;
    private String calorieRange;
    private String origin;

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

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getDiet() {
        return diet;
    }

    public void setDiet(String diet) {
        this.diet = diet;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getCalorieRange() {
        return calorieRange;
    }

    public void setCalorieRange(String calorieRange) {
        this.calorieRange = calorieRange;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }
}
