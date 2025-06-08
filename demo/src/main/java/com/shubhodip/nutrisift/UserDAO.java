package com.shubhodip.nutrisift;

public interface UserDAO {
    int saveUser(User user);
    User getUserByEmail(String email);
    boolean emailExists(String email);
    boolean isUserIdExists(String userid);
    boolean updatePassword(String email, String newPassword);
    boolean updateLastLogin(String email);
}
