package com.brandex.ui;

import com.brandex.App;
import com.brandex.models.User;
import com.brandex.service.AuthService;
import com.brandex.utilities.ImageLoader;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

// The controller class for handling the profile view.
public class ProfileController {

    @FXML
    private ImageView profileImageView;
    @FXML
    private Label fullNameLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label phoneLabel;
    @FXML
    private Label addressLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private Label statusLabel;

    // Initializes the profile view
    @FXML
    public void initialize() {
        User user = AuthService.getInstance().getCurrentUser();
        if (user != null) {
            fullNameLabel.setText(user.getFirstName() + " " + user.getLastName());
            usernameLabel.setText("@" + user.getUsername());
            emailLabel.setText(user.getEmail());

            phoneLabel.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "Not provided");
            addressLabel.setText(user.getShippingAddress() != null ? user.getShippingAddress() : "No address set");
            roleLabel.setText(user.getRole().toUpperCase());
            statusLabel.setText(user.getStatus().name());

            ImageLoader.load(profileImageView, user.getProfileImgURL());

            Circle clip = new Circle(
                    profileImageView.getFitWidth() / 2,
                    profileImageView.getFitHeight() / 2,
                    profileImageView.getFitWidth() / 2);
            profileImageView.setClip(clip);
        }
    }

    // Handles logging out
    @FXML
    public void logout() {
        AuthService.getInstance().logout();
        App.setRoot("main");
    }

    // Handles editing the profile
    @FXML
    private void handleEditProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/brandex/fxml/store/ProfileForm.fxml"));
            VBox root = loader.load();

            ProfileFormController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Profile");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(profileImageView.getScene().getWindow());

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            if (controller.isSaved()) {
                initialize();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error opening profile form: " + e.getMessage());
        }
    }

    // Handles changing the password
    @FXML
    private void handleChangePassword() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/brandex/fxml/store/ProfilePasswordForm.fxml"));
            VBox root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Change Password");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(profileImageView.getScene().getWindow());

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            initialize();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error opening password form: " + e.getMessage());
        }
    }
}
