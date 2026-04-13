package com.brandex.ui;

import com.brandex.models.User;
import com.brandex.repository.UserRepository;
import com.brandex.service.AuthService;
import com.brandex.utilities.StatusLabelHelper;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

// The controller class for handling the profile form view.
public class ProfileFormController {

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField imageUrlField;
    @FXML
    private Label errorLabel;

    private User currentUser;
    private boolean saved = false;

    // Initializes the profile form view
    @FXML
    public void initialize() {
        this.currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser != null) {
            firstNameField.setText(currentUser.getFirstName() != null ? currentUser.getFirstName() : "");
            lastNameField.setText(currentUser.getLastName() != null ? currentUser.getLastName() : "");
            usernameField.setText(currentUser.getUsername() != null ? currentUser.getUsername() : "");
            emailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
            phoneField.setText(currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "");
            addressField.setText(currentUser.getShippingAddress() != null ? currentUser.getShippingAddress() : "");
            imageUrlField.setText(currentUser.getProfileImgURL() != null ? currentUser.getProfileImgURL() : "");
        }
    }

    // Returns true if the profile was saved
    public boolean isSaved() {
        return saved;
    }

    // Handles saving the profile
    @FXML
    private void handleSave() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            StatusLabelHelper.showError(errorLabel, "First name and last name are required.");
            return;
        }

        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);
        currentUser.setPhoneNumber(phoneField.getText().trim().isEmpty() ? null : phoneField.getText().trim());
        currentUser.setShippingAddress(addressField.getText().trim().isEmpty() ? null : addressField.getText().trim());
        currentUser.setProfileImgURL(imageUrlField.getText().trim().isEmpty() ? null : imageUrlField.getText().trim());

        UserRepository userRepo = UserRepository.getInstance();
        boolean success = userRepo.updateUser(currentUser);

        if (success) {
            this.saved = true;
            closeStage();
        } else {
            StatusLabelHelper.showError(errorLabel, "Failed to update profile. Please try again.");
        }
    }

    // Handles cancelling the profile form view
    @FXML
    private void handleCancel() {
        closeStage();
    }

    // Closes the stage
    private void closeStage() {
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        stage.close();
    }
}
