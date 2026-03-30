package com.brandex.ui;

import com.brandex.App;
import com.brandex.models.User;
import com.brandex.service.AuthService;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private StackPane contentArea;
    @FXML private HBox adminControls;

    User user = AuthService.getInstance().getCurrentUser();

    // this runs automatically when Dashboard.fxml loads
    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, " + user.getFirstName());
            if (user.getRole().equals("admin")) {
                adminControls.setVisible(true);
                adminControls.setManaged(true);
            } else {
                adminControls.setVisible(false);
                adminControls.setManaged(false);
            }
    }
}
