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

    // Private constructor for singleton pattern
    private AuthService() {
    }

    // Returns the instance of the AuthService
    public static AuthService getInstance() {
        if (instance == null)
            instance = new AuthService();
        return instance;
    }

    // Returns the current user
    public User getCurrentUser() {
        return this.currentUser;
    }

    // Returns the logged-in user, or throws with a readable message
    public User login(String email, String password) {

        User user = this.userRepo.findByEmail(email);

        if (user == null || !PasswordHasher.matches(password, user.getPasswordHash()))
            throw new IllegalArgumentException("Invalid email or password.");

        this.currentUser = user;
        return user;
    }

    // Returns the OTP so you can email it
    public void register(String firstName, String lastName, String email, String username) throws Exception {
        User existingEmail = this.userRepo.findByEmail(email);
        User existingUser = this.userRepo.findByUsername(username);

        if (existingEmail != null && existingEmail.isOtpUsed())
            throw new IllegalArgumentException("An account with this email already exists.");
        if (existingUser != null && existingUser.isOtpUsed())
            throw new IllegalArgumentException("An account with this username already exists.");

        // If we reach here, any existing user is an orphaned registration
        User user = existingEmail != null ? existingEmail : (existingUser != null ? existingUser : new User());

        String otp = OTPGenerator.generate();
        String otpHash = PasswordHasher.hash(otp);

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash(otpHash);
        user.setOtpHash(otpHash);
        user.setOtpUsed(false);
        user.setForcePwChange(true);
        user.setRole("customer");

        this.currentUser = user;

        if (user.getId() == null) {
            // New user
            String userId = this.userRepo.createUser(user);
            if (userId == null)
                throw new Exception("Failed to register user.");
            user.setId(userId);
            CartService.getInstance().createCart(user.getId());
        } else {
            // Updating orphaned user
            this.userRepo.updateUser(user);
            this.userRepo.updateOtp(user.getId(), otpHash);
        }

        // Send the welcome email asynchronously with retries
        EmailSender.sendAsync(
                user.getEmail(),
                "Welcome to BrandEx!",
                buildWelcomeEmail(user, otp),
                () -> System.out.println("Welcome email sent to " + user.getEmail()),
                error -> System.err.println("Welcome email PERMANENT failure: " + error));
    }

    // Verifies the OTP and marks it as used if valid
    public void verifyOtp(String username, String enteredOtp) {

        User user = this.userRepo.findByUsername(username);

        if (user == null || !PasswordHasher.matches(enteredOtp, user.getOtpHash()))
            throw new IllegalArgumentException("Invalid OTP.");

        if (user.isOtpUsed())
            throw new IllegalArgumentException("OTP has already been used.");

        this.userRepo.updateOtpUsed(user.getId());
    }

    // Resends the OTP to the user
    public void resendOtp(String username) throws Exception {
        User user = this.userRepo.findByUsername(username);
        if (user == null)
            throw new IllegalArgumentException("User not found.");
        if (user.isOtpUsed())
            throw new IllegalArgumentException("OTP has already been used. You are already verified.");

        String otp = OTPGenerator.generate();
        String otpHash = PasswordHasher.hash(otp);

        user.setOtpHash(otpHash);
        user.setPasswordHash(otpHash);
        user.setOtpUsed(false);

        this.userRepo.updateUser(user);
        this.userRepo.updateOtp(user.getId(), otpHash);

        if (this.currentUser != null && this.currentUser.getId().equals(user.getId())) {
            this.currentUser = user;
        }

        // Send an async welcome/new OTP email with retries
        EmailSender.sendAsync(
                user.getEmail(),
                "Your New BrandEx Code",
                buildWelcomeEmail(user, otp),
                () -> System.out.println("Resend OTP email sent to " + user.getEmail()),
                error -> System.err.println("Resend OTP email PERMANENT failure: " + error));
    }

    // Changes the user's password
    public void changePassword(String username, String oldPassword, String newPassword) {

        User user = this.userRepo.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        if (!PasswordHasher.matches(oldPassword, user.getPasswordHash()))
            throw new IllegalArgumentException("Current password is incorrect.");

        String newHash = PasswordHasher.hash(newPassword);

        if (newHash.equals(user.getPasswordHash()) || newHash.equals(user.getPrevHash1())
                || newHash.equals(user.getPrevHash2()))
            throw new IllegalArgumentException("New password cannot match your last two passwords.");

        this.userRepo.updatePassword(user.getId(), newHash, user.getPasswordHash(), user.getPrevHash1());
        user.setForcePwChange(false);

        if (user.isOtpUsed() && !user.isForcePwChange()
                && user.getStatus() == com.brandex.models.enums.UserStatus.PENDING) {
            this.userRepo.updateUserStatusAndRole(user.getId(), com.brandex.models.enums.UserStatus.ACTIVE,
                    user.getRole());
            user.setStatus(com.brandex.models.enums.UserStatus.ACTIVE);
        }

        if (this.currentUser != null && this.currentUser.getId().equals(user.getId())) {
            this.currentUser.setPasswordHash(newHash);
            this.currentUser.setForcePwChange(false);
            if (user.getStatus() == com.brandex.models.enums.UserStatus.ACTIVE) {
                this.currentUser.setStatus(com.brandex.models.enums.UserStatus.ACTIVE);
            }
        }
    }

    // Builds the welcome email
    private String buildWelcomeEmail(User user, String otp) {
        return String.format("""
                Hello %s,

                Welcome to BrandEx! We're excited to have you on board.

                ─────────────────────────────────────
                YOUR TEMPORARY ACCESS
                ─────────────────────────────────────
                Username      :  %s
                One-Time PW   :  %s

                Please note: You will be required to change your password
                immediately upon your first login for security purposes.

                ─────────────────────────────────────
                WHAT'S NEXT?
                ─────────────────────────────────────
                1. Log in with your temporary password.
                2. Set up your private profile.
                3. Explore our product catalog!

                Thank you for choosing BrandEx!

                Regards,
                The BrandEx Team
                """,
                user.getFirstName(),
                user.getUsername(),
                otp);
    }

    // Logs out the current user
    public void logout() {
        this.currentUser = null;
        CartService.getInstance().clearCart();
        ProductService.getInstance().clearProducts();
        UserService.getInstance().clearUsers();
        OrderService.getInstance().clearOrders();
    }
}
