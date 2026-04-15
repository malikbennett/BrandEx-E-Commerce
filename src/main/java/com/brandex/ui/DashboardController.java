package com.brandex.ui;

import com.brandex.models.User;
import com.brandex.service.AuthService;
import com.brandex.service.ProductService;

import atlantafx.base.controls.ModalPane;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

// The controller class for handling the dashboard.
public class DashboardController {

    private static DashboardController instance;

    @FXML
    private Label welcomeLabel;
    @FXML
    private StackPane contentArea;
    @FXML
    private javafx.scene.layout.VBox adminControls;
    @FXML
    private TextField searchField;
    @FXML
    private ModalPane modalPane;

    User user = AuthService.getInstance().getCurrentUser();

    // Product catalog button
    @FXML
    private void showProducts() {
        loadView("store/ProductCatalog");
    }

    // Home button
    @FXML
    void showHome() {
        searchField.setText("");
        handleSearch();
    }

    // Cart button
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

    // Order history button
    @FXML
    private void showOrders() {
        loadView("store/OrderHistory");
    }

    // Profile button
    @FXML
    private void showProfile() {
        loadView("store/Profile");
    }

    // Manage products button
    @FXML
    private void showManageProducts() {
        loadView("admin/ManageProducts");
    }

    // Manage orders button
    @FXML
    private void showManageOrders() {
        loadView("admin/ManageOrders");
    }

    // Manage users button
    @FXML
    private void showManageUsers() {
        loadView("admin/ManageUsers");
    }

    // Product form button
    @FXML
    private void showProductForm() {
        loadView("admin/ProductForm");
    }

    // Search button
    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        ProductService.getInstance().setSearchQuery(query);
        showProducts();
    }

    // allows for different views to be loaded
    public static void loadView(String fxml) {
        if (instance != null) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        instance.getClass().getResource("/com/brandex/fxml/" + fxml + ".fxml"));
                Node view = loader.load();
                instance.contentArea.getChildren().setAll(view);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // this runs automatically when Dashboard.fxml loads
    @FXML
    public void initialize() {
        instance = this;

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
