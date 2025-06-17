package com.shubhodip.nutrisift.user;

public interface UserDAO {
    int saveUser(User user);
    User getUserByEmail(String email);
    Profile getProfileByEmail(String email);
    int updateProfile(Profile profile);
    boolean emailExists(String email);
    boolean isUserIdExists(String userid);
    boolean updatePassword(String email, String newPassword);
    boolean updateLastLogin(String email);
    int saveProfile(Profile profile);
}
