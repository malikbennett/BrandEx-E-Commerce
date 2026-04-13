package com.brandex.ui;

import com.brandex.service.AuthService;
import com.brandex.utilities.InputValidator;
import com.brandex.utilities.StatusLabelHelper;
import com.brandex.models.User;
import com.brandex.App;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

// The controller class for handling authentication.
public class AuthController {
    @FXML
    private TextField loginEmailField;
    @FXML
    private PasswordField loginPasswordField;
    @FXML
    private Label loginErrorLabel;
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
    @FXML
    private TextField otpField;
    @FXML
    private Label otpErrorLabel;
    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label changePasswordErrorLabel;
    private final AuthService authService = AuthService.getInstance();

    // Handles user login, verifies credentials, and navigates to the appropriate
    // screen
    @FXML
    private void handleLogin() {

        String email = loginEmailField.getText().trim();
        String password = loginPasswordField.getText();

        try {
            User user = authService.login(email, password);

            if (!user.isOtpUsed()) {
                App.setRoot("auth/OTPVerify");
            } else if (user.isForcePwChange()) {
                App.setRoot("auth/ChangePassword");
            } else {
                App.setRoot("store/Dashboard");
            }

        } catch (com.brandex.database.DatabaseException e) {
            StatusLabelHelper.showError(loginErrorLabel, "Database connection failed. Please try again later.");
        } catch (Exception e) {
            StatusLabelHelper.showError(loginErrorLabel, e.getMessage());
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
            StatusLabelHelper.showError(registerErrorLabel, "Please fill in all fields.");
            return;
        }
        // Added email format validation using InputValidator utility class
        if (!InputValidator.isValidEmail(email)) {
            StatusLabelHelper.showError(registerErrorLabel, "Please enter a valid email address.");
            return;
        }
        try {
            // Call the register method and get the generated OTP
            authService.register(firstName, lastName, email, username);
            // Navigate to OTP verification screen
            App.setRoot("auth/OTPVerify");
        } catch (com.brandex.database.DatabaseException e) {
            StatusLabelHelper.showError(registerErrorLabel, "Database connection failed. Please try again later.");
        } catch (Exception e) {
            StatusLabelHelper.showError(registerErrorLabel, e.getMessage());
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
        } catch (com.brandex.database.DatabaseException e) {
            StatusLabelHelper.showError(otpErrorLabel, "Database connection failed. Please try again later.");
        } catch (Exception e) {
            StatusLabelHelper.showError(otpErrorLabel, e.getMessage());
        }
    }

    // Resends the OTP to the user
    @FXML
    private void handleResendOtp() {
        if (authService.getCurrentUser() == null)
            return;
        String username = authService.getCurrentUser().getUsername();
        try {
            authService.resendOtp(username);
            StatusLabelHelper.showSuccess(otpErrorLabel, "A new OTP has been sent to your email.");
        } catch (com.brandex.database.DatabaseException e) {
            StatusLabelHelper.showError(otpErrorLabel, "Database connection failed.");
        } catch (Exception e) {
            StatusLabelHelper.showError(otpErrorLabel, e.getMessage());
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
            System.out.println(username);
            authService.changePassword(username, currentPassword, newPassword);
            App.setRoot("store/Dashboard");
        } catch (com.brandex.database.DatabaseException e) {
            StatusLabelHelper.showError(changePasswordErrorLabel, "Database connection failed.");
        } catch (Exception e) {
            StatusLabelHelper.showError(changePasswordErrorLabel, e.getMessage());
        }
    }
}
