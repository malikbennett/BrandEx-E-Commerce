package com.brandex.ui;

import com.brandex.App;
import com.brandex.models.User;
import com.brandex.service.AuthService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private StackPane contentArea;
    @FXML private HBox adminControls;

    User user = AuthService.getInstance().getCurrentUser();
    // Shared views
    @FXML private void showProducts() { loadView("shared/ProductCatalog"); }
    // Customer views
    @FXML private void showCart()     { loadView("customer/Cart"); }
    @FXML private void showOrders()   { loadView("customer/OrderHistory"); }
    @FXML private void showProfile()  { loadView("customer/Profile"); }
    // Admin views
    @FXML private void showManageProducts()   { loadView("admin/ManageProducts"); }
    @FXML private void showManageOrders()     { loadView("admin/ManageOrders"); }
    @FXML private void showManageUsers()  { loadView("admin/ManageUsers"); }
    @FXML private void showProductForm()  { loadView("admin/ProductForm"); }

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/brandex/fxml/" + fxml + ".fxml")
            );
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        loadView("shared/ProductCatalog");
    }
}
