package com.brandex.ui;

import com.brandex.service.AuthService;
import com.brandex.utilities.EmailSender;
import com.brandex.utilities.InputValidator;
import com.brandex.utilities.ThrowError;
import com.brandex.models.User;
import com.brandex.App;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

public class AuthController {
    // Login fields
    @FXML
    private TextField loginEmailField;
    @FXML
    private PasswordField loginPasswordField;
    @FXML
    private Label loginErrorLabel;
    // Registration fields
    @FXML
    private TextField registerEmailField;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField usernameField;
    @FXML
    private Label registerErrorLabel;
    // OTP verification field
    @FXML
    private TextField otpField;
    @FXML
    private Label otpErrorLabel;
    // Change password fields
    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label changePasswordErrorLabel;
    // AuthService instance for handling authentication logic
    private final AuthService authService = AuthService.getInstance();

    @FXML
    private void handleLogin() {

        String email = loginEmailField.getText().trim();
        String password = loginPasswordField.getText();

        try {
            User user = authService.login(email, password);

            if (user.isForcePwChange()) {
                App.setRoot("auth/ChangePassword");
            } else {
                App.setRoot("store/Dashboard");
            }

        } catch (Exception e) {
            ThrowError.errorLabel(loginErrorLabel, e.getMessage());
        }
    }

    // Handles user registration, generates OTP, saves user with OTP hash, and sends
    // OTP email
    @FXML
    private void handleRegister() {

        String email = registerEmailField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String username = usernameField.getText().trim();

        // Basic input validation for empty fields and email format
        if (firstName.isEmpty() || lastName.isEmpty()) {
            ThrowError.errorLabel(registerErrorLabel, "Please fill in all fields.");
            return;
        }
        // Added email format validation using InputValidator utility class
        if (!InputValidator.isValidEmail(email)) {
            ThrowError.errorLabel(registerErrorLabel, "Please enter a valid email address.");
            return;
        }
        try {
            // Call the register method and get the generated OTP
            authService.register(firstName, lastName, email, username);
            // Navigate to OTP verification screen
            App.setRoot("auth/OTPVerify");
        } catch (Exception e) {
            ThrowError.errorLabel(registerErrorLabel, e.getMessage());
        }
    }

    // Verifies the entered OTP against the stored hash and marks it as used if
    // valid
    @FXML
    private void handleVerifyOtp() {

        String otp = otpField.getText().trim();
        String username = authService.getCurrentUser().getUsername();

        try {
            authService.verifyOtp(username, otp);
            App.setRoot("auth/ChangePassword");
        } catch (Exception e) {
            ThrowError.errorLabel(otpErrorLabel, e.getMessage());
        }
    }

    // Handles password change, checks for matching new passwords and password
    // history, and updates the password
    @FXML
    private void handleChangePassword() {

        String currentPassword = currentPasswordField.getText().trim();
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String username = authService.getCurrentUser().getUsername();

        try {
            if (!newPassword.equals(confirmPassword)) {
                throw new Exception("New password and confirmation do not match.");
            }
            authService.changePassword(username, currentPassword, newPassword);
            App.setRoot("store/Dashboard");
        } catch (Exception e) {
            ThrowError.errorLabel(changePasswordErrorLabel, e.getMessage());
        }
    }

    @FXML
    private void initialize() {
        if (loginEmailField != null)
            loginEmailField.setText("brandex.project@gmail.com");
        if (loginPasswordField != null)
            loginPasswordField.setText("password");
    }
}
