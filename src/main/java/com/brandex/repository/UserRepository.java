package com.brandex.repository;

import com.brandex.models.User;
import com.brandex.models.enums.UserStatus;
import com.brandex.database.JDBC;
import com.brandex.datastructures.LinkedList;

import java.sql.*;
import java.time.OffsetDateTime;

// The repository class for managing users.
public class UserRepository {

    private static UserRepository instance;

    // Returns the instance of the UserRepository
    public static UserRepository getInstance() {
        if (instance == null)
            instance = new UserRepository();
        return instance;
    }

    // Returns a list of all users
    public LinkedList<User> listUsers() {
        LinkedList<User> users = new LinkedList<>((a, b) -> 0);
        String sql = "SELECT * FROM users";
        try (ResultSet rs = JDBC.query(sql)) {
            while (rs.next()) {
                users.insert(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new com.brandex.database.DatabaseException("Failed to list users", e);
        }
        return users;
    }

    // Returns a user based on the condition and value
    public User findById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?::uuid";
        try (ResultSet rs = JDBC.query(sql, id)) {
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new com.brandex.database.DatabaseException("Failed to find user by ID: " + id, e);
        }
        return null;
    }

    // Returns a user based on the condition and value
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (ResultSet rs = JDBC.query(sql, username)) {
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new com.brandex.database.DatabaseException("Failed to find user by username: " + username, e);
        }
        return null;
    }

    // Returns a user based on the condition and value
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (ResultSet rs = JDBC.query(sql, email)) {
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new com.brandex.database.DatabaseException("Failed to find user by email: " + email, e);
        }
        return null;
    }

    // Creates a new user
    public String createUser(User user) {
        String sql = "INSERT INTO users (username, email, first_name, last_name, password_hash, role, otp_hash, otp_used, force_pw_change) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (ResultSet rs = JDBC.query(sql,
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPasswordHash(),
                user.getRole(),
                user.getOtpHash(),
                user.isOtpUsed(),
                user.isForcePwChange())) {
            if (rs.next())
                return rs.getString("id");
        } catch (SQLException e) {
            throw new com.brandex.database.DatabaseException("Failed to create user: " + user.getEmail(), e);
        }
        return null;
    }

    // Updates a user
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET username=?, email=?, first_name=?, last_name=?, phone_number=?, shipping_address=?, status=?, role=?, password_hash=?, profile_image_url=? WHERE id=?::uuid";
        JDBC.execute(sql,
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getShippingAddress(),
                user.getStatus().name(),
                user.getRole(),
                user.getPasswordHash(),
                user.getProfileImgURL(),
                user.getId());
        return true;
    }

    public boolean updatePassword(String userId, String newHash, String prev1, String prev2) {
        String sql = "UPDATE users SET password_hash = ?, prev_hash_1 = ?, prev_hash_2 = ?, force_pw_change = false WHERE id = ?::uuid";
        JDBC.execute(sql, newHash, prev1, prev2, userId);
        return true;
    }

    public boolean updateOtpUsed(String userId) {
        String sql = "UPDATE users SET otp_used = true WHERE id = ?::uuid";
        JDBC.execute(sql, userId);
        return true;
    }

    public boolean updateOtp(String userId, String newOtpHash) {
        String sql = "UPDATE users SET otp_hash = ?, otp_used = false WHERE id = ?::uuid";
        JDBC.execute(sql, newOtpHash, userId);
        return true;
    }

    public boolean updateUserStatusAndRole(String userId, UserStatus status, String role) {
        String sql = "UPDATE users SET status = ?, role = ? WHERE id = ?::uuid";
        JDBC.execute(sql, status.name(), role, userId);
        return true;
    }

    public boolean resetPassword(String userId, String tempPwHash) {
        String sql = "UPDATE users SET password_hash = ?, force_pw_change = true WHERE id = ?::uuid";
        JDBC.execute(sql, tempPwHash, userId);
        return true;
    }

    public boolean deleteUser(String userId) {
        String sql = "DELETE FROM users WHERE id = ?::uuid";
        JDBC.execute(sql, userId);
        return true;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getString("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setShippingAddress(rs.getString("shipping_address"));
        user.setStatus(UserStatus.valueOf(rs.getString("status").toUpperCase()));
        user.setRole(rs.getString("role"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setPrevHash1(rs.getString("prev_hash_1"));
        user.setPrevHash2(rs.getString("prev_hash_2"));
        user.setOtpHash(rs.getString("otp_hash"));
        user.setOtpUsed(rs.getBoolean("otp_used"));
        boolean forcePw = rs.getBoolean("force_pw_change");
        if (rs.wasNull()) {
            forcePw = true;
        }
        user.setForcePwChange(forcePw);
        user.setProfileImgURL(rs.getString("profile_image_url"));
        user.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
        return user;
    }
}
