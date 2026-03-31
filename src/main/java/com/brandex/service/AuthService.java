package com.brandex.service;

import com.brandex.models.User;
import com.brandex.repository.UserRepository;
import com.brandex.utilities.OTPGenerator;
import com.brandex.utilities.PasswordHasher;

// Handles user registration, login, OTP verification, and password changes
public class AuthService {

    private static AuthService instance;
    private final UserRepository userRepo = UserRepository.getInstance();
    private User currentUser;

    private AuthService() {
    }

    public static AuthService getInstance() {
        if (instance == null)
            instance = new AuthService();
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    // Returns the logged-in user, or throws with a readable message
    public User login(String email, String password) throws Exception {

        User user = userRepo.getUser("email", email);

        if (user == null)
            throw new Exception("Email not found.");

        if (!PasswordHasher.matches(password, user.getPasswordHash()))
            throw new Exception("Incorrect password.");

        currentUser = user;
        return user;
    }

    // Returns the OTP so you can email it
    public String register(String firstName, String lastName, String email, String username) throws Exception {

        if (userRepo.getUser("email", email) != null)
            throw new Exception("An account with this email already exists.");

        if (userRepo.getUser("username", username) != null)
            throw new Exception("An account with this username already exists.");

        String otp = OTPGenerator.generate();
        String otpHash = PasswordHasher.hash(otp);

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash(otpHash);
        user.setOtpHash(otpHash);
        user.setOtpUsed(false);
        user.setForcePwChange(true);
        user.setRole("customer");

        userRepo.createUser(user);
        currentUser = user;
        return otp;
    }

    // Verifies the OTP and marks it as used if valid
    public void verifyOtp(String username, String enteredOtp) throws Exception {

        User user = userRepo.getUser("username", username);

        if (user == null || !PasswordHasher.matches(enteredOtp, user.getOtpHash()))
            throw new Exception("Invalid OTP.");

        if (user.isOtpUsed())
            throw new Exception("OTP has already been used.");

        userRepo.updateOtpUsed(username);
    }

    // Changes the user's password
    public void changePassword(String username, String oldPassword, String newPassword) throws Exception {

        User user = userRepo.getUser("username", username);

        if (!PasswordHasher.matches(oldPassword, user.getPasswordHash()))
            throw new Exception("Current password is incorrect.");

        String newHash = PasswordHasher.hash(newPassword);

        if (newHash.equals(user.getPasswordHash()) || newHash.equals(user.getPrevHash1())
                || newHash.equals(user.getPrevHash2()))
            throw new Exception("New password cannot match your last two passwords.");

        userRepo.updatePassword(username, newHash, user.getPasswordHash(), user.getPrevHash1());

        if (currentUser != null && currentUser.getUsername().equals(username))
            currentUser.setPasswordHash(newHash);
    }

    public void logout() {
        currentUser = null;
    }
}
