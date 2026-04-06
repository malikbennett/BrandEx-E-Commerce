package com.brandex.ui;

import javafx.scene.control.Label;

import com.brandex.models.CartItem;
import com.brandex.models.Product;
import com.brandex.service.CartService;
import com.brandex.service.ProductService;
import com.brandex.utilities.ImageLoader;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.controls.Tile;
import atlantafx.base.theme.Styles;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Spinner;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class CartController {
    private ModalPane modalPane;
    private Node cartView;

    @FXML
    private Label cartTotal;

    @FXML
    private VBox cartBox;

    CartService cartService = CartService.getInstance();
    ProductService productService = ProductService.getInstance();

    public void initialize() {
        loadCart();
    }

    public void buildCartModal(CartItem cart) {
        Product product = productService.searchById(cart.getProductId());
        // the tile itself, has graphic, title, description, and action sections
        Tile tile = new Tile();
        tile.getStyleClass().add(Styles.ELEVATED_1);

        // image
        ImageView imgView = new ImageView();
        imgView.setFitWidth(150);
        imgView.setFitHeight(108);
        imgView.setPreserveRatio(true);
        imgView.setSmooth(true);
        imgView.setStyle("-fx-background-color: transparent;");
        ImageLoader.loadProductImage(imgView, product.getImageUrl());
        // wrap image in a container to apply rounded corners for the header
        StackPane imageContainer = new StackPane(imgView);
        imageContainer.setMinHeight(108);
        imageContainer.setMaxHeight(108);
        imageContainer.setStyle("-fx-background-color: #f5f5f5;" + "-fx-background-radius: 8 8 8 8;");
        StackPane.setAlignment(imgView, Pos.CENTER);
        tile.setGraphic(imageContainer);

        // title
        tile.setTitle(product.getName());

        // description
        tile.setDescription(product.getDescription());

        // action
        Label priceLabel = new Label(String.format("$%.2f", cart.getTotalPrice()));
        priceLabel.getStyleClass().add(Styles.TEXT_BOLD);

        Spinner<Integer> quantitySpinner = new Spinner<>(0, 100, cart.getQuantity());
        quantitySpinner.setPrefWidth(100);

        HBox actionBox = new HBox(15, priceLabel, quantitySpinner);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        quantitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal < oldVal) {
                cartService.removeItem(cart.getProductId(), oldVal - newVal);
                loadCart();
            } else if (newVal > oldVal) {
                cartService.addItem(cart.getProductId(), newVal - oldVal);
                loadCart();
            }
        });

        tile.setAction(actionBox);

        cartBox.getChildren().add(tile);

        cartTotal.setText(String.format("Total: $%.2f", cartService.getCartTotal()));
    }

    public void loadCart() {
        if (cartService.getCartItems().isEmpty()) {
            cartService.loadCart();
        }
        cartBox.getChildren().clear();

        try {
            cartService.getCartItems().traverse(cartItem -> {
                buildCartModal(cartItem);
            });
        } catch (Exception e) {
            System.err.println("Error loading items in Cart: " + e.getMessage());
            e.printStackTrace();
        }

        if (cartTotal != null) {
            cartTotal.setText(String.format("Total: $%.2f", cartService.getCartTotal()));
        }
    }

    @FXML
    private void checkOutCart() {
        CartService.getInstance().checkout();
        closeCart();
    }

    @FXML
    private void closeCart() {
        if (modalPane != null) {
            modalPane.hide(true);
        }
    }

    @FXML
    private void undoCart() {
        CartService.getInstance().undo();
        loadCart();
    }

    @FXML
    private void redoCart() {
        CartService.getInstance().redo();
        loadCart();
    }

    public void setModalPane(ModalPane modalPane, Node cartView) {
        this.modalPane = modalPane;
        this.cartView = cartView;
        modalPane.setAlignment(Pos.CENTER_RIGHT);
        modalPane.usePredefinedTransitionFactories(Side.RIGHT);
        modalPane.show(this.cartView);
    }
}
