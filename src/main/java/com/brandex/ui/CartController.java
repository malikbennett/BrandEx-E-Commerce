package com.brandex.ui;

import com.brandex.models.CartItem;
import com.brandex.models.Product;
import com.brandex.service.CartService;
import com.brandex.service.ProductService;
import com.brandex.utilities.ImageLoader;

import atlantafx.base.controls.Card;
import atlantafx.base.controls.ModalPane;
import atlantafx.base.controls.Tile;
import atlantafx.base.theme.Styles;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class CartController {
    CartService cartService = CartService.getInstance();
    ProductService productService = ProductService.getInstance();

    private ModalPane modalPane;
    private Node cartView;

    @FXML
    private VBox cartBox;

    public void initialize() {
        System.out.println("CartController initialized");
        loadCart();
    }

    public void buildCartModal(CartItem cart) {
        Product product = productService.searchById(cart.getProductId());
        // the tile itself, has graphic, title, description, and action sections
        Tile tile = new Tile();
        tile.getStyleClass().add(Styles.ELEVATED_1);

        // image
        ImageView imgView = new ImageView();
        imgView.setFitWidth(250);
        imgView.setFitHeight(180);
        imgView.setPreserveRatio(true);
        imgView.setSmooth(true);
        imgView.setStyle("-fx-background-color: transparent;");
        ImageLoader.loadProductImage(imgView, product.getImageUrl());
        // wrap image in a container to apply rounded corners for the header
        StackPane imageContainer = new StackPane(imgView);
        imageContainer.setMinHeight(180);
        imageContainer.setMaxHeight(180);
        imageContainer.setStyle("-fx-background-color: #f5f5f5;" + "-fx-background-radius: 8 8 8 8;");
        StackPane.setAlignment(imgView, Pos.CENTER);
        tile.setGraphic(imageContainer);

        // title
        tile.setTitle(product.getName());

        // description
        tile.setDescription(product.getDescription());

        // action
        // Button removeButton = new Button("Remove");
        // removeButton.setOnAction(e -> {
        // cartService.removeItem(cart.getProductId(), cart.getQty());
        // loadCart();
        // });
        // tile.setAction(removeButton);

        cartBox.getChildren().add(tile);

    }

    public void loadCart() {

        cartBox.getChildren().clear();

        try {
            cartService.getCartItems().traverse(cartItem -> {
                buildCartModal(cartItem);
            });
        } catch (Exception e) {
            System.err.println("Error loading items in Cart: " + e.getMessage());
            e.printStackTrace();
        }
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
