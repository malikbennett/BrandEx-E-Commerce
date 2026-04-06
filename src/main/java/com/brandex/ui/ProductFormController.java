package com.brandex.ui;

import com.brandex.models.Product;
import com.brandex.service.AuthService;
import com.brandex.service.ProductService;

import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * ProductFormController
 *
 * Handles both ADD and EDIT in a single form.
 *
 * - When opened via "Add Product":  selectedProduct == null  → calls createProduct()
 * - When opened via "Edit" button:  loadProduct(p) is called → calls updateProduct()
 *
 * After a successful save, onSaveCallback.run() is called so the caller
 * (ManageProductsController) can refresh its list.
 *
 * FXML path: /com/brandex/fxml/admin/ProductForm.fxml
 */
public class ProductFormController {

    // ── Form fields ───────────────────────────────────────────────────────────
    @FXML private Label     formTitle;
    @FXML private TextField tfName;
    @FXML private TextField tfBrand;
    @FXML private TextField tfCategory;
    @FXML private TextField tfPrice;
    @FXML private TextField tfStock;
    @FXML private TextField tfRating;
    @FXML private TextField tfImageUrl;
    @FXML private TextArea  taDescription;

    // ── Feedback ──────────────────────────────────────────────────────────────
    @FXML private Label statusLabel;

    // ── Internal state ────────────────────────────────────────────────────────
    private Product  selectedProduct;   // null = add mode, non-null = edit mode
    private String   originalName;      // name before edits – needed for BST removal
    private Runnable onSaveCallback;    // called after successful save

    private final ProductService productService = ProductService.getInstance();

    @FXML
    public void initialize() {
        // Guard: only admins may reach this screen
        if (!AuthService.getInstance().getCurrentUser().getRole().equals("admin")) {
            disableAllFields();
            showStatus("Admin access required.", true);
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Pre-fills the form with an existing product's data (edit mode).
     * Must be called AFTER FXMLLoader.load().
     */
    public void loadProduct(Product product) {
        this.selectedProduct = product;
        this.originalName    = product.getName();   // snapshot before any edits

        formTitle.setText("Edit Product");

        tfName.setText(product.getName());
        tfBrand.setText(nullSafe(product.getBrand()));
        tfCategory.setText(nullSafe(product.getCategory()));
        tfPrice.setText(String.valueOf(product.getPrice()));
        tfStock.setText(String.valueOf(product.getStock()));
        tfRating.setText(String.valueOf(product.getRating()));
        tfImageUrl.setText(nullSafe(product.getImageUrl()));
        taDescription.setText(nullSafe(product.getDescription()));
    }

    /** Called by ManageProductsController so we can refresh its list on save. */
    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    // ── Save handler ──────────────────────────────────────────────────────────

    @FXML
    private void handleSave() {
        if (!validateInputs()) return;

        if (selectedProduct == null) {
            doCreate();
        } else {
            doUpdate();
        }
    }

    private void doCreate() {
        Product p = new Product();
        applyFormTo(p);

        try {
            productService.createProduct(p);          // DB → BST → LinkedList
            showStatus("Product \"" + p.getName() + "\" created successfully.", false);
            clearForm();
            if (onSaveCallback != null) onSaveCallback.run();
        } catch (Exception e) {
            showStatus("Error creating product: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void doUpdate() {
        applyFormTo(selectedProduct);

        try {
            productService.updateProduct(selectedProduct, originalName); // DB → BST → LinkedList
            showStatus("Product updated successfully.", false);
            originalName = selectedProduct.getName();   // refresh snapshot
            if (onSaveCallback != null) onSaveCallback.run();
        } catch (Exception e) {
            showStatus("Error updating product: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    // ── Cancel / Back ─────────────────────────────────────────────────────────

    @FXML
    private void handleCancel() {
        navigateTo("admin/ManageProducts");
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        if (tfName.getText().isBlank())
            errors.append("• Product name is required.\n");

        try {
            double price = Double.parseDouble(tfPrice.getText().trim());
            if (price < 0) errors.append("• Price must be non-negative.\n");
        } catch (NumberFormatException e) {
            errors.append("• Price must be a valid number.\n");
        }

        try {
            int stock = Integer.parseInt(tfStock.getText().trim());
            if (stock < 0) errors.append("• Stock must be non-negative.\n");
        } catch (NumberFormatException e) {
            errors.append("• Stock must be a valid whole number.\n");
        }

        if (!tfRating.getText().isBlank()) {
            try {
                double rating = Double.parseDouble(tfRating.getText().trim());
                if (rating < 0 || rating > 5)
                    errors.append("• Rating must be between 0 and 5.\n");
            } catch (NumberFormatException e) {
                errors.append("• Rating must be a valid number.\n");
            }
        }

        // Basic injection guard
        String[] textInputs = {
            tfName.getText(), tfBrand.getText(),
            tfCategory.getText(), taDescription.getText()
        };
        for (String val : textInputs) {
            if (val != null && (val.contains(";") || val.contains("--"))) {
                errors.append("• Input contains illegal characters (; or --).\n");
                break;
            }
        }

        if (!errors.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please fix the following:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }
        return true;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Copies current form values onto the given Product object. */
    private void applyFormTo(Product p) {
        p.setName(tfName.getText().trim());
        p.setBrand(emptyToNull(tfBrand.getText()));
        p.setCategory(emptyToNull(tfCategory.getText()));
        p.setPrice(Double.parseDouble(tfPrice.getText().trim()));
        p.setStock(Integer.parseInt(tfStock.getText().trim()));
        p.setRating(tfRating.getText().isBlank() ? 0.0
                : Double.parseDouble(tfRating.getText().trim()));
        p.setImageUrl(emptyToNull(tfImageUrl.getText()));
        p.setDescription(emptyToNull(taDescription.getText()));
    }

    private void clearForm() {
        tfName.clear(); tfBrand.clear(); tfCategory.clear();
        tfPrice.clear(); tfStock.clear(); tfRating.clear();
        tfImageUrl.clear(); taDescription.clear();
        selectedProduct = null;
        originalName    = null;
        formTitle.setText("Add Product");
    }

    private void showStatus(String msg, boolean isError) {
        Platform.runLater(() -> {
            statusLabel.setText(msg);
            statusLabel.setTextFill(isError ? Color.web("#e74c3c") : Color.web("#2ecc71"));
            statusLabel.setVisible(true);
        });
    }

    private void disableAllFields() {
        for (javafx.scene.control.Control c : new javafx.scene.control.Control[]{
            tfName, tfBrand, tfCategory, tfPrice, tfStock,
            tfRating, tfImageUrl, taDescription
        }) c.setDisable(true);
    }

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/brandex/fxml/" + fxmlPath + ".fxml"));
            Node view = loader.load();
            StackPane contentArea = getContentArea();
            if (contentArea != null) contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private StackPane getContentArea() {
        Node node = tfName;
        while (node != null) {
            node = node.getParent();
            if (node instanceof StackPane sp && sp.getId() != null
                    && sp.getId().equals("contentArea")) {
                return sp;
            }
        }
        return null;
    }

    private String nullSafe(String s)    { return s != null ? s : ""; }
    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}