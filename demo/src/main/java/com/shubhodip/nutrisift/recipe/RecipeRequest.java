package com.shubhodip.nutrisift.recipe;

// (Assuming your RecipeRequest class looks like this)
public class RecipeRequest {
    private String ingredients;
    private String mail;
    private String uid; // Add this field if not present

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}