package com.shubhodip.nutrisift.recipe;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ApiUsageDAOImpl implements ApiUsageDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public int getApiUsageCount(String uid, String email, String endpoint, String date) {
        String sql = "SELECT count FROM api_usage_logs WHERE user_id = ? AND email = ? AND endpoint = ? AND usage_date = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, uid, email, endpoint, date);
        } catch (Exception e) {
            // No record found or other error
            return 0;
        }
    }

    @Override
    @Transactional
    public void incrementApiUsage(String uid, String email, String endpoint, String date) {
        // First check if record exists
        String selectSql = "SELECT count FROM api_usage_logs WHERE user_id = ? AND email = ? AND endpoint = ? AND usage_date = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(selectSql, Integer.class, uid, email, endpoint, date);
            if (count != null) {
                // Record exists, update it
                String updateSql = "UPDATE api_usage_logs SET count = count + 1 WHERE user_id = ? AND email = ? AND endpoint = ? AND usage_date = ?";
                jdbcTemplate.update(updateSql, uid, email, endpoint, date);
            } else {
                // Record doesn't exist, insert new one
                insertNewUsageRecord(uid, email, endpoint, date);
            }
        } catch (Exception e) {
            // Record doesn't exist, insert new one
            insertNewUsageRecord(uid, email, endpoint, date);
        }
    }
    
    private void insertNewUsageRecord(String uid, String email, String endpoint, String date) {
        String insertSql = "INSERT INTO api_usage_logs (user_id, email, endpoint, usage_date, count) VALUES (?, ?, ?, ?, 1)";
        jdbcTemplate.update(insertSql, uid, email, endpoint, date);
    }

    @Override
    @Transactional
    public boolean checkAndUpdateApiUsage(String uid, String email, String endpoint, int limit) {
        String today = LocalDate.now().format(DATE_FORMATTER);
        int currentCount = getApiUsageCount(uid, email, endpoint, today);
        
        if (currentCount >= limit) {
            return false; // Limit exceeded
        }
        
        // Increment usage count
        incrementApiUsage(uid, email, endpoint, today);
        return true;
    }

    @Override
    public void trackApiUsage(String uid, String email, String endpoint) {
        String today = LocalDate.now().format(DATE_FORMATTER);
        incrementApiUsage(uid, email, endpoint, today);
    }
}