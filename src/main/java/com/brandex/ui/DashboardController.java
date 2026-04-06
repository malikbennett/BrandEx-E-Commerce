package com.brandex.ui;

import com.brandex.models.User;
import com.brandex.service.AuthService;
import com.brandex.service.CartService;
import com.brandex.service.ProductService;

import atlantafx.base.controls.ModalPane;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class DashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private StackPane contentArea;
    @FXML
    private HBox adminControls;
    @FXML
    private TextField searchField;
    @FXML
    private ModalPane modalPane;

    User user = AuthService.getInstance().getCurrentUser();

    // Shared views
    @FXML
    private void showProducts() {
        loadView("store/ProductCatalog");
    }

    @FXML
    void showHome() {
        searchField.setText("");
        handleSearch();
    }

    @FXML
    private void showCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/brandex/fxml/store/Cart.fxml"));
            Node cartView = loader.load();
            CartController controller = loader.getController();
            controller.setModalPane(this.modalPane, cartView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showOrders() {
        loadView("store/OrderHistory");
    }

    @FXML
    private void showProfile() {
        loadView("store/Profile");
    }

    // Admin views
    @FXML
    private void showManageProducts() {
        loadView("admin/ManageProducts");
    }

    @FXML
    private void showManageOrders() {
        loadView("admin/ManageOrders");
    }

    @FXML
    private void showManageUsers() {
        loadView("admin/ManageUsers");
    }

    @FXML
    private void showProductForm() {
        loadView("admin/ProductForm");
    }

    // allows for different views to be loaded
    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/brandex/fxml/" + fxml + ".fxml"));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        ProductService.getInstance().setSearchQuery(query);
        showProducts();
    }

    // this runs automatically when Dashboard.fxml loads
    @FXML
    public void initialize() {

        // sets the welcome label to the current user's first name
        welcomeLabel.setText("Welcome, " + user.getFirstName());

        // checks if the current user is an admin
        if (user.getRole().equals("admin")) {
            adminControls.setVisible(true);
            adminControls.setManaged(true);
        } else {
            adminControls.setVisible(false);
            adminControls.setManaged(false);

        }
        showProducts();
    }
}
