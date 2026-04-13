package com.brandex.ui;

import java.time.format.DateTimeFormatter;

import com.brandex.models.Product;
import com.brandex.models.User;
import com.brandex.service.AuthService;
import com.brandex.service.ProductService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

// The controller class for handling the manage products view.
public class ManageProductsController {

    @FXML
    private TableView<Product> productsTable;
    @FXML
    private TableColumn<Product, String> idColumn;
    @FXML
    private TableColumn<Product, String> nameColumn;
    @FXML
    private TableColumn<Product, String> brandColumn;
    @FXML
    private TableColumn<Product, String> descriptionColumn;
    @FXML
    private TableColumn<Product, String> stockColumn;
    @FXML
    private TableColumn<Product, String> priceColumn;
    @FXML
    private TableColumn<Product, String> categoryColumn;
    @FXML
    private TableColumn<Product, String> ratingColumn;
    @FXML
    private TableColumn<Product, String> imageURLColumn;
    @FXML
    private TableColumn<Product, String> dateCreatedColumn;
    @FXML
    private TableColumn<Product, Void> actionsColumn;
    @FXML
    private javafx.scene.control.TextField searchField;

    @FXML
    private Button addProductBtn;

    private final ObservableList<Product> allProducts = FXCollections.observableArrayList();
    private final ProductService productService = ProductService.getInstance();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    // Initializes the manage products view
    @FXML
    public void initialize() {
        User currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser == null || !currentUser.getRole().equalsIgnoreCase("admin")) {
            productsTable.setDisable(true);
            addProductBtn.setDisable(true);
            return;
        }

        setupTable();
        loadProducts();
        setupSearch();
    }

    // Sets up the table
    private void setupTable() {
        idColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        brandColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBrand()));
        descriptionColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        stockColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getStock())));
        priceColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getPrice())));
        categoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));
        ratingColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getRating())));
        imageURLColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getImageUrl()));
        dateCreatedColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new SimpleStringProperty(cellData.getValue().getCreatedAt().format(formatter));
            }
            return new SimpleStringProperty("N/A");
        });

        setupActionsColumn();
    }

    // Sets up the actions column
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox container = new HBox(editBtn, deleteBtn);

            {
                container.setAlignment(Pos.CENTER);
                editBtn.getStyleClass().add("btn-edit");
                deleteBtn.getStyleClass().add("btn-delete");

                editBtn.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleEditProduct(product);
                });

                deleteBtn.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleDelete(product);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    // Loads the products
    private void loadProducts() {
        if (!productService.isLoaded())
            productService.loadProducts();

        allProducts.clear();
        productService.forEachProduct(product -> this.allProducts.add(product));
        productsTable.setItems(allProducts);
    }

    // Sets up the search
    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.isBlank()) {
                productsTable.setItems(allProducts);
                return;
            }
            String query = val.toLowerCase();
            ObservableList<Product> filtered = FXCollections.observableArrayList();
            for (Product product : allProducts) {
                if (product.getName().toLowerCase().contains(query)
                        || product.getBrand().toLowerCase().contains(query)
                        || product.getCategory().toLowerCase().contains(query)
                        || product.getId().toLowerCase().contains(query)) {
                    filtered.add(product);
                }
            }
            productsTable.setItems(filtered);
        });
    }

    // Handles adding a product
    @FXML
    private void handleAddProduct() {
        showProductForm(null);
    }

    // Handles editing a product
    private void handleEditProduct(Product product) {
        showProductForm(product);
    }

    // Shows the product form
    private void showProductForm(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/brandex/fxml/admin/ProductForm.fxml"));
            VBox root = loader.load();

            ProductFormController controller = loader.getController();
            controller.setProduct(product);

            Stage dialogStage = new Stage();
            dialogStage.setTitle(product == null ? "Add Product" : "Edit Product - " + product.getName());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(productsTable.getScene().getWindow());

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            if (controller.isSaved()) {
                loadProducts();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error opening product form: " + e.getMessage());
        }
    }

    // Handles deleting a product
    private void handleDelete(Product product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Product: " + product.getName());
        alert.setContentText("Are you sure you want to delete this product? This action cannot be undone.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                productService.deleteProduct(product);
                loadProducts();
            } catch (Exception e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setHeaderText("Failed to Delete Product");
                error.setContentText(e.getMessage());
                error.showAndWait();
            }
        }
    }

    // Handles refreshing the products
    @FXML
    private void handleRefresh() {
        productService.reloadProducts();
        loadProducts();
        searchField.clear(); // Clear search on explicit refresh
    }
}
