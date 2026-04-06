package com.brandex.ui;

import java.time.format.DateTimeFormatter;

import com.brandex.models.Product;
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
    private Button addProductBtn;

    private final ProductService productService = ProductService.getInstance();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @FXML
    public void initialize() {
        System.out.println("ManageProductsController initialized");
        setupTable();
        loadProducts();
    }

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

    private void loadProducts() {
        if (productService.getProductsTree().isEmpty()) {
            productService.loadProducts();
        }

        ObservableList<Product> products = FXCollections.observableArrayList();
        productService.forEachProduct(products::add);
        productsTable.setItems(products);
    }

    @FXML
    private void handleAddProduct() {
        showProductForm(null);
    }

    private void handleEditProduct(Product product) {
        showProductForm(product);
    }

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
}
