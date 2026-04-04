package com.brandex.ui;

import com.brandex.models.Product;
import com.brandex.service.ProductService;

import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * ManageProductsController
 *
 * Admin-only view that lists all products in a scrollable table.
 * Each row has Edit and Delete action buttons.
 *
 * FXML path: /com/brandex/fxml/admin/ManageProducts.fxml
 */
public class ManageProductsController {

    @FXML private VBox   productListBox;   // rows are injected here
    @FXML private Label  statusLabel;      // feedback messages

    private final ProductService productService = ProductService.getInstance();

    @FXML
    public void initialize() {
        refreshList();
    }

    // ── Build / Refresh ───────────────────────────────────────────────────────

    /**
     * Clears and rebuilds the product list from the in-memory LinkedList.
     * Called on load and after every create/update/delete operation.
     */
    public void refreshList() {
        productListBox.getChildren().clear();

        productService.getAllProducts().traverse(product -> {
            productListBox.getChildren().add(buildRow(product));
        });

        if (productListBox.getChildren().isEmpty()) {
            Label empty = new Label("No products found. Use 'Add Product' to create one.");
            empty.getStyleClass().add(Styles.TEXT_MUTED);
            empty.setPadding(new Insets(24));
            productListBox.getChildren().add(empty);
        }
    }

    // ── Row builder ───────────────────────────────────────────────────────────

    private HBox buildRow(Product product) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 16, 10, 16));
        row.getStyleClass().addAll("product-row");

        // ── Name + category ──
        VBox nameBox = new VBox(2);
        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add(Styles.TEXT_BOLD);
        Label catLabel  = new Label(product.getCategory() != null ? product.getCategory() : "—");
        catLabel.getStyleClass().add(Styles.TEXT_MUTED);
        nameBox.getChildren().addAll(nameLabel, catLabel);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        // ── Brand ──
        Label brandLabel = new Label(product.getBrand() != null ? product.getBrand() : "—");
        brandLabel.setMinWidth(90);

        // ── Price ──
        Label priceLabel = new Label(String.format("$%.2f", product.getPrice()));
        priceLabel.setMinWidth(70);

        // ── Stock (colour-coded) ──
        Label stockLabel = new Label(product.getStock() + " in stock");
        stockLabel.setMinWidth(90);
        if (product.getStock() == 0)
            stockLabel.setTextFill(Color.web("#e74c3c"));
        else if (product.getStock() <= 5)
            stockLabel.setTextFill(Color.web("#f39c12"));

        // ── Action buttons ──
        Button editBtn   = new Button("Edit");
        Button deleteBtn = new Button("Delete");
        editBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        deleteBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.DANGER);
        editBtn.setMinWidth(60);
        deleteBtn.setMinWidth(70);

        editBtn.setOnAction(e -> handleEdit(product));
        deleteBtn.setOnAction(e -> handleDelete(product));

        HBox actions = new HBox(8, editBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(nameBox, brandLabel, priceLabel, stockLabel, actions);
        return row;
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    /** Navigate to the ProductForm pre-populated with this product's data. */
    private void handleEdit(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/brandex/fxml/admin/ProductForm.fxml"));
            Node formView = loader.load();

            ProductFormController formController = loader.getController();
            formController.loadProduct(product);           // pre-fill the form
            formController.setOnSaveCallback(this::refreshList); // refresh table after save

            // Replace the content area (walk up to DashboardController's StackPane)
            StackPane contentArea = getContentArea();
            if (contentArea != null) contentArea.getChildren().setAll(formView);

        } catch (Exception e) {
            showStatus("Error opening edit form: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    /** Show a confirmation dialog then delete. */
    private void handleDelete(Product product) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Product");
        confirm.setHeaderText("Delete \"" + product.getName() + "\"?");
        confirm.setContentText(
                "This will permanently remove the product from the database\n"
              + "and all in-memory structures. This cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    productService.deleteProduct(product);       // DB + BST + LinkedList
                    showStatus("\"" + product.getName() + "\" deleted successfully.", false);
                    refreshList();
                } catch (Exception ex) {
                    showStatus("Delete failed: " + ex.getMessage(), true);
                    ex.printStackTrace();
                }
            }
        });
    }

    // ── Navigate to Add Product form ──────────────────────────────────────────

    @FXML
    private void handleAddProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/brandex/fxml/admin/ProductForm.fxml"));
            Node formView = loader.load();

            ProductFormController formController = loader.getController();
            formController.setOnSaveCallback(this::refreshList);  // refresh on return

            StackPane contentArea = getContentArea();
            if (contentArea != null) contentArea.getChildren().setAll(formView);

        } catch (Exception e) {
            showStatus("Error opening add form: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void showStatus(String msg, boolean isError) {
        Platform.runLater(() -> {
            statusLabel.setText(msg);
            statusLabel.setTextFill(isError ? Color.web("#e74c3c") : Color.web("#2ecc71"));
            statusLabel.setVisible(true);
        });
    }

    /**
     * Walks up the scene graph to find the DashboardController's contentArea
     * StackPane so we can swap views without needing a direct reference.
     */
    private StackPane getContentArea() {
        Node node = productListBox;
        while (node != null) {
            node = node.getParent();
            if (node instanceof StackPane sp && sp.getId() != null
                    && sp.getId().equals("contentArea")) {
                return sp;
            }
        }
        return null;
    }
}