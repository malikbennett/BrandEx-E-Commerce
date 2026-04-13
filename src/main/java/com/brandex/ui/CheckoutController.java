package com.brandex.ui;

import com.brandex.models.Product;
import com.brandex.models.enums.PaymentMethod;
import com.brandex.service.CartService;
import com.brandex.service.ProductService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

// The controller class for handling the checkout process.
public class CheckoutController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextArea addressField;
    @FXML
    private ComboBox<PaymentMethod> paymentComboBox;
    @FXML
    private TextField cardNumberField;
    @FXML
    private VBox cardDetailsBox;
    @FXML
    private Label errorLabel;

    @FXML
    private VBox orderSummaryBox;
    @FXML
    private Label subtotalLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private Button placeOrderBtn;

    @FXML
    private VBox successOverlay;
    @FXML
    private Label successMessage;

    private final CartService cartService = CartService.getInstance();
    private final ProductService productService = ProductService.getInstance();

    // Initializes the checkout view
    @FXML
    public void initialize() {
        paymentComboBox.setItems(FXCollections.observableArrayList(PaymentMethod.values()));
        paymentComboBox.getSelectionModel().selectFirst();

        // Only show card mock details if a card is selected
        paymentComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isCard = (newVal == PaymentMethod.CREDIT_CARD || newVal == PaymentMethod.DEBIT_CARD);
            cardDetailsBox.setVisible(isCard);
            cardDetailsBox.setManaged(isCard);
        });

        loadOrderSummary();
    }

    // Loads the order summary
    private void loadOrderSummary() {
        orderSummaryBox.getChildren().clear();
        cartService.getCartItems().traverse(item -> {
            Product p = productService.searchById(item.getProductId());
            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);
            Label nameQty = new Label(String.format("%s (x%d)", p.getName(), item.getQuantity()));
            nameQty.setMaxWidth(160);
            nameQty.setWrapText(true);
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            Label price = new Label(String.format("$%.2f", item.getTotalPrice()));
            row.getChildren().addAll(nameQty, spacer, price);
            orderSummaryBox.getChildren().add(row);
        });

        double total = cartService.getCartTotal();
        subtotalLabel.setText(String.format("$%.2f", total));
        totalLabel.setText(String.format("$%.2f", total));
    }

    // Uses the default values from the current user
    @FXML
    private void handleUseDefaultValues() {
        com.brandex.models.User user = com.brandex.service.AuthService.getInstance().getCurrentUser();
        if (user != null) {
            String fullName = user.getFullName();
            if (fullName != null && !fullName.isBlank()) {
                nameField.setText(fullName.trim());
            }
            String phone = user.getPhoneNumber();
            if (phone != null && !phone.isBlank()) {
                phoneField.setText(phone.trim());
            }
            String address = user.getShippingAddress();
            if (address != null && !address.isBlank()) {
                addressField.setText(address.trim());
            }
        }
    }

    // Places the order
    @FXML
    private void handlePlaceOrder() {
        errorLabel.setText("");

        String name = nameField.getText();
        String phone = phoneField.getText();
        String address = addressField.getText();
        PaymentMethod method = paymentComboBox.getValue();

        if (name == null || name.isBlank()) {
            errorLabel.setText("Please enter your full name.");
            return;
        }
        if (address == null || address.isBlank()) {
            errorLabel.setText("Please enter a shipping address.");
            return;
        }

        if (method == PaymentMethod.CREDIT_CARD || method == PaymentMethod.DEBIT_CARD) {
            String card = cardNumberField.getText();
            if (card == null || card.isBlank() || card.length() < 12) {
                errorLabel.setText("Please enter a valid card number.");
                return;
            }
        }

        try {
            String finalAddress = name + "\n" + address;
            if (phone != null && !phone.isBlank()) {
                finalAddress += "\nPhone: " + phone;
            }
            cartService.checkout(finalAddress, method);

            // Show Success Overlay
            successMessage.setText(
                    "We've successfully processed your payment. Your order is now placed in the fulfillment queue.");
            successOverlay.setVisible(true);

        } catch (Exception e) {
            errorLabel.setText("Error completing checkout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Continues to the product catalog
    @FXML
    private void handleContinueShopping() {
        DashboardController.loadView("store/ProductCatalog");
    }
}
