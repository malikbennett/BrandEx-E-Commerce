package com.brandex.service;

import com.brandex.models.User;
import com.brandex.repository.UserRepository;
import com.brandex.utilities.EmailSender;
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
    public User login(String email, String password) {

        User user = userRepo.findByEmail(email);

        if (user == null || !PasswordHasher.matches(password, user.getPasswordHash()))
            throw new IllegalArgumentException("Invalid email or password.");

        currentUser = user;
        return user;
    }

    // Returns the OTP so you can email it
    public void register(String firstName, String lastName, String email, String username) throws Exception {

        if (userRepo.findByEmail(email) != null || userRepo.findByUsername(username) != null)
            throw new IllegalArgumentException("An account with this email or username already exists.");

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

        String userId = userRepo.createUser(user);
        if (userId == null) {
            throw new Exception("Failed to register user.");
        }
        user.setId(userId);
        currentUser = user; 
        
        CartService.getInstance().createCart();

        // Send the OTP to the user's email
        String subject = "Welcome to BrandEx!";
        String body = String.format("""
                Hello %s,

                Welcome to BrandEx!
                Your one-time password is: %s

                Regards,
                The BrandEx Team
                """, user.getFirstName(), otp);
        EmailSender.send(user.getEmail(), subject, body);
    }

    // Verifies the OTP and marks it as used if valid
    public void verifyOtp(String username, String enteredOtp) {

        User user = userRepo.findByUsername(username);

        if (user == null || !PasswordHasher.matches(enteredOtp, user.getOtpHash()))
            throw new IllegalArgumentException("Invalid OTP.");

        if (user.isOtpUsed())
            throw new IllegalArgumentException("OTP has already been used.");

        userRepo.updateOtpUsed(user.getId());
    }

    // Changes the user's password
    public void changePassword(String username, String oldPassword, String newPassword) {

        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        if (!PasswordHasher.matches(oldPassword, user.getPasswordHash()))
            throw new IllegalArgumentException("Current password is incorrect.");

        String newHash = PasswordHasher.hash(newPassword);

        if (newHash.equals(user.getPasswordHash()) || newHash.equals(user.getPrevHash1())
                || newHash.equals(user.getPrevHash2()))
            throw new IllegalArgumentException("New password cannot match your last two passwords.");

        userRepo.updatePassword(user.getId(), newHash, user.getPasswordHash(), user.getPrevHash1());

        if (currentUser != null && currentUser.getId().equals(user.getId()))
            currentUser.setPasswordHash(newHash);
    }

    public void logout() {
        currentUser = null;
        CartService.getInstance().clearCart();
        ProductService.getInstance().clearProducts();
        UserService.getInstance().clearUsers();
    }
}
