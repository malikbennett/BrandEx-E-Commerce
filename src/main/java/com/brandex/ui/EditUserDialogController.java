package com.brandex.ui;

import com.brandex.models.User;
import com.brandex.models.enums.UserStatus;
import com.brandex.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

// The controller class for handling the edit user dialog.
public class EditUserDialogController {

    @FXML
    private Label userInfoLabel;
    @FXML
    private ComboBox<UserStatus> statusComboBox;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private Button resetPasswordBtn;

    private User user;
    private final UserService userService = UserService.getInstance();
    private boolean saved = false;

    // Sets the user to be edited
    public void setUser(User user) {
        this.user = user;
        userInfoLabel.setText(String.format("User: %s (%s)", user.getUsername(), user.getEmail()));
        statusComboBox.setValue(user.getStatus());
        roleComboBox.setValue(user.getRole().substring(0, 1).toUpperCase() + user.getRole().substring(1).toLowerCase());
    }

    // Initializes the dialog
    @FXML
    public void initialize() {
        statusComboBox.getItems().setAll(UserStatus.values());
        roleComboBox.getItems().setAll("Admin", "Customer");
    }

    // Saves the user
    @FXML
    private void handleSave() {
        try {
            userService.updateUser(user, statusComboBox.getValue(), roleComboBox.getValue().toLowerCase());
            saved = true;
            closeStage();
        } catch (Exception e) {
            System.err.println("Failed to save user: " + e.getMessage());
        }
    }

    // Cancels the edit
    @FXML
    private void handleCancel() {
        closeStage();
    }

    // Resets the user's password
    @FXML
    private void handleResetPassword() {
        try {
            userService.resetPassword(user);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Password Reset");
            alert.setHeaderText(null);
            alert.setContentText("A temporary password has been sent to " + user.getEmail());
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to Reset Password");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    // Checks if the user was saved
    public boolean isSaved() {
        return saved;
    }

    // Closes the dialog
    private void closeStage() {
        Stage stage = (Stage) userInfoLabel.getScene().getWindow();
        stage.close();
    }
}
