package com.shubhodip.nutrisift;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserDAOImpl implements UserDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public int saveUser(User user) {
        String sql = "INSERT INTO users (userid, name, email, phone, password, security_question, answer) VALUES (?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql, user.getUserid(), user.getName(), user.getEmail(), 
                           user.getPhone(), user.getPassword(), user.getSecurityQuestion(), user.getSecurityAnswer());
    }

    @Override
    public User getUserByEmail(String email) {
        try {
            String sql = "SELECT * FROM users WHERE email = ?";
            return jdbcTemplate.queryForObject(sql, new UserRowMapper(), email);
        } catch (EmptyResultDataAccessException e) {
            return null; // User not found
        }
    }

    @Override
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    @Override
    public boolean isUserIdExists(String userid) {
        String sql = "SELECT COUNT(*) FROM users WHERE userid = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userid);
        return count != null && count > 0;
    }

    @Override
    public boolean updatePassword(String email, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE email = ?";
        try {
            int rowsAffected = jdbcTemplate.update(sql, newPassword, email);
            if (rowsAffected > 0) {
                System.out.println("Password updated successfully for email: " + email);
                return true;
            } else {
                System.out.println("No user found with email: " + email);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error updating password for email " + email + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateLastLogin(String email) {
        // Set the timezone to Indian Standard Time (IST)
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        
        // Get current timestamp in IST
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(System.currentTimeMillis());
        
        String sql = "UPDATE users SET last_login = ? WHERE email = ?";
        try {
            int rowsAffected = jdbcTemplate.update(sql, currentTimestamp, email);
            if (rowsAffected > 0) {
                System.out.println("Last login updated successfully for email: " + email);
                return true;
            } else {
                System.out.println("No user found with email: " + email);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error updating last login for email " + email + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setUserid(rs.getString("userid"));
            user.setName(rs.getString("name"));
            user.setEmail(rs.getString("email"));
            user.setPhone(rs.getString("phone"));
            user.setPassword(rs.getString("password"));
            user.setSecurityQuestion(rs.getString("security_question"));
            user.setSecurityAnswer(rs.getString("answer")); // Note the column name is "answer"
            return user;
        }
    }
}
