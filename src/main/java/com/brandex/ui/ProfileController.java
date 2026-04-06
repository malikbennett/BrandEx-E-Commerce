package com.brandex.ui;

import com.brandex.App;
import com.brandex.models.User;
import com.brandex.service.AuthService;
import com.brandex.utilities.ImageLoader;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

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

    @FXML
    public void logout() {
        AuthService.getInstance().logout();
        App.setRoot("main");
    }

    @FXML
    private void handleEditProfile() {
        System.out.println("Edit profile clicked");
    }

    @FXML
    private void handleChangePassword() {
        System.out.println("Change password clicked");
    }
}
