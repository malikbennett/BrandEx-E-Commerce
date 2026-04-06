package com.brandex.ui;

import com.brandex.App;
import com.brandex.service.AuthService;

import javafx.fxml.FXML;

public class ProfileController {
    @FXML
    public void logout() {
        AuthService.getInstance().logout();
        App.setRoot("main");
    }
}
