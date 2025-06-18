package com.shubhodip.nutrisift.recipe;

public interface ApiUsageDAO {
    int getApiUsageCount(String uid, String email, String endpoint, String date);
    void incrementApiUsage(String uid, String email, String endpoint, String date);
    boolean checkAndUpdateApiUsage(String uid, String email, String endpoint, int limit);
    void trackApiUsage(String uid, String email, String endpoint);
}