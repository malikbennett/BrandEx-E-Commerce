package com.brandex.ui;

import com.brandex.service.AuthService;
import com.brandex.utilities.StatusLabelHelper;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

// The controller class for handling the profile password form view.
public class ProfilePasswordFormController {

    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label errorLabel;

    // Handles saving the profile password form view
    @FXML
    private void handleSave() {
        String currentPassword = currentPasswordField.getText().trim();
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            StatusLabelHelper.showError(errorLabel, "Please fill in all fields.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            StatusLabelHelper.showError(errorLabel, "New password and confirmation do not match.");
            return;
        }

        try {
            String username = AuthService.getInstance().getCurrentUser().getUsername();
            AuthService.getInstance().changePassword(username, currentPassword, newPassword);

            closeStage();
        } catch (com.brandex.database.DatabaseException e) {
            StatusLabelHelper.showError(errorLabel, "Database error: Failed to update password.");
        } catch (Exception e) {
            StatusLabelHelper.showError(errorLabel, e.getMessage());
        }
    }

    // Handles cancelling the profile password form view
    @FXML
    private void handleCancel() {
        closeStage();
    }

    // Closes the stage
    private void closeStage() {
        Stage stage = (Stage) currentPasswordField.getScene().getWindow();
        stage.close();
    }
}
