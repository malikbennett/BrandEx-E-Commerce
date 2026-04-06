package com.brandex.ui;

import com.brandex.models.Product;
import com.brandex.service.ProductService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ProductFormController {

    @FXML
    private Label productInfoLabel;
    @FXML
    private TextField nameField;
    @FXML
    private TextField brandField;
    @FXML
    private TextField categoryField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField stockField;
    @FXML
    private TextField ratingField;
    @FXML
    private TextField imageUrlField;
    @FXML
    private TextArea descriptionField;

    private Product product;
    private String originalName;
    private final ProductService productService = ProductService.getInstance();
    private boolean saved = false;

    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            this.originalName = product.getName();
            productInfoLabel.setText(String.format("Product: %s (%s)", product.getName(), product.getBrand()));
            nameField.setText(product.getName());
            brandField.setText(nullSafe(product.getBrand()));
            categoryField.setText(nullSafe(product.getCategory()));
            priceField.setText(String.valueOf(product.getPrice()));
            stockField.setText(String.valueOf(product.getStock()));
            ratingField.setText(String.valueOf(product.getRating()));
            imageUrlField.setText(nullSafe(product.getImageUrl()));
            descriptionField.setText(nullSafe(product.getDescription()));
        } else {
            productInfoLabel.setText("Adding New Product");
            this.product = new Product();
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return;
        }

        try {
            if (originalName == null) {
                // Creation mode
                applyFormToProduct();
                productService.createProduct(product);
            } else {
                // Update mode (safely handles BST key change)
                productService.updateProduct(product, originalName, this::applyFormToProduct);
            }
            saved = true;
            closeStage();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error Saving Product", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    public boolean isSaved() {
        return saved;
    }

    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        if (nameField.getText().isBlank()) {
            errors.append("- Product name is required.\n");
        }

        try {
            double price = Double.parseDouble(priceField.getText().trim());
            if (price < 0) errors.append("- Price must be non-negative.\n");
        } catch (NumberFormatException e) {
            errors.append("- Price must be a valid number.\n");
        }

        try {
            int stock = Integer.parseInt(stockField.getText().trim());
            if (stock < 0) errors.append("- Stock must be non-negative.\n");
        } catch (NumberFormatException e) {
            errors.append("- Stock must be a valid whole number.\n");
        }

        if (!ratingField.getText().isBlank()) {
            try {
                double rating = Double.parseDouble(ratingField.getText().trim());
                if (rating < 0 || rating > 5) errors.append("- Rating must be between 0 and 5.\n");
            } catch (NumberFormatException e) {
                errors.append("- Rating must be a valid number.\n");
            }
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please correct the following errors:\n" + errors.toString());
            return false;
        }
        return true;
    }

    private void applyFormToProduct() {
        product.setName(nameField.getText().trim());
        product.setBrand(emptyToNull(brandField.getText()));
        product.setCategory(emptyToNull(categoryField.getText()));
        product.setPrice(Double.parseDouble(priceField.getText().trim()));
        product.setStock(Integer.parseInt(stockField.getText().trim()));
        product.setRating(ratingField.getText().isBlank() ? 0.0 : Double.parseDouble(ratingField.getText().trim()));
        product.setImageUrl(emptyToNull(imageUrlField.getText()));
        product.setDescription(emptyToNull(descriptionField.getText()));
    }

    private void closeStage() {
        Stage stage = (Stage) productInfoLabel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String nullSafe(String s) {
        return (s == null) ? "" : s;
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
