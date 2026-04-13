package com.brandex.ui;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.brandex.models.Product;
import com.brandex.service.CartService;
import com.brandex.service.ProductService;
import com.brandex.utilities.ImageLoader;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

// The controller class for handling the product catalog view.
public class ProductCatalogController {

    @FXML
    private TilePane productGrid;

    @FXML
    private VBox priceFilterBox;
    @FXML
    private VBox priceSliderBox;
    @FXML
    private Slider minPriceSlider;
    @FXML
    private Slider maxPriceSlider;
    @FXML
    private Label priceLabelMin;
    @FXML
    private Label priceLabelMax;
    @FXML
    private VBox categoryFilterBox;
    @FXML
    private VBox categoryCheckboxBox;
    @FXML
    private VBox brandFilterBox;
    @FXML
    private VBox brandCheckboxBox;

    private final ProductService productService = ProductService.getInstance();

    // Initializes the product catalog view
    @FXML
    public void initialize() {
        loadProducts();
        loadFilters();
    }

    // Builds a product card
    private void buildProductCard(Product product) {
        VBox card = new VBox(15);
        card.getStyleClass().add("product-card");
        card.setMinWidth(220);
        card.setMaxWidth(220);
        card.setAlignment(Pos.TOP_CENTER);

        // image
        ImageView imgView = new ImageView();
        imgView.setFitWidth(180);
        imgView.setFitHeight(130);
        imgView.setPreserveRatio(true);
        imgView.setSmooth(true);
        ImageLoader.loadProductImage(imgView, product.getImageUrl());

        StackPane imageContainer = new StackPane(imgView);
        imageContainer.setMinHeight(150);
        imageContainer.setStyle("-fx-background-color: #1c1c1e; -fx-background-radius: 10;");
        StackPane.setAlignment(imgView, Pos.CENTER);

        VBox info = new VBox(5);
        info.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(product.getName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 15;");
        name.setWrapText(true);

        Label brand = new Label(product.getBrand());
        brand.setStyle("-fx-text-fill: -brand-text-dim; -fx-font-size: 12;");

        HBox priceBox = new HBox();
        priceBox.setAlignment(Pos.CENTER_LEFT);
        Label price = new Label(String.format("$%.2f", product.getPrice()));
        price.setStyle("-fx-font-weight: 900; -fx-font-size: 16; -fx-text-fill: -brand-primary;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button addToCart = new Button("Add");
        addToCart.getStyleClass().add("btn-brand");
        addToCart.setOnAction(e -> {
            CartService.getInstance().addItem(product.getId(), 1);
        });

        priceBox.getChildren().addAll(price, spacer, addToCart);
        info.getChildren().addAll(name, brand, priceBox);

        card.getChildren().addAll(imageContainer, info);
        productGrid.getChildren().add(card);
    }

    // Loads the products
    private void loadProducts() {
        if (!productService.isLoaded()) {
            productService.loadProducts();
        }
        productGrid.getChildren().clear();

        try {
            productService.searchByKeyword("").traverse(product -> {
                buildProductCard(product);
            });
        } catch (Exception e) {
            System.err.println("Error loading products in Catalog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Loads the filters
    private void loadFilters() {
        brandCheckboxBox.getChildren().clear();
        categoryCheckboxBox.getChildren().clear();

        // a set stores only unique elements
        Set<String> brands = new HashSet<>();
        Set<String> categories = new HashSet<>();

        try {
            // update label when either slider moves
            minPriceSlider.valueProperty().addListener((obs, old, newVal) -> {
                // prevent min from going above max
                if (newVal.doubleValue() > maxPriceSlider.getValue())
                    minPriceSlider.setValue(maxPriceSlider.getValue());
                updatePriceLabel();
            });

            maxPriceSlider.valueProperty().addListener((obs, old, newVal) -> {
                // prevent max from going below min
                if (newVal.doubleValue() < minPriceSlider.getValue())
                    maxPriceSlider.setValue(minPriceSlider.getValue());
                updatePriceLabel();
            });

            minPriceSlider.setOnMouseReleased(e -> handleFilters());
            maxPriceSlider.setOnMouseReleased(e -> handleFilters());

            // gets all brands and categories for filters
            ProductService.getInstance().searchByKeyword("").traverse(product -> {
                if (product.getBrand() != null)
                    brands.add(product.getBrand());
                if (product.getCategory() != null)
                    categories.add(product.getCategory());
            });
            // create a checkbox for each unique brand
            brands.stream().sorted().forEach(brand -> {
                CheckBox cb = new CheckBox(brand);
                cb.setOnAction(e -> handleFilters());
                brandCheckboxBox.getChildren().add(cb);
            });
            // create a checkbox for each unique category
            categories.stream().sorted().forEach(category -> {
                CheckBox cb = new CheckBox(category);
                cb.setOnAction(e -> handleFilters());
                categoryCheckboxBox.getChildren().add(cb);
            });
        } catch (Exception e) {
            System.err.println("Error filtering products in Catalog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Updates the price label
    private void updatePriceLabel() {
        priceLabelMin.setText(String.format("$%.0f", minPriceSlider.getValue()));
        priceLabelMax.setText(String.format("$%.0f", maxPriceSlider.getValue()));
    }

    // Handles filtering the products
    @FXML
    private void handleFilters() {
        try {

            // get selected brands
            Set<String> selectedBrands = brandCheckboxBox.getChildren().stream()
                    .filter(node -> node instanceof CheckBox) // filter only checkboxes
                    .map(node -> (CheckBox) node) // cast to CheckBox
                    .filter(CheckBox::isSelected) // filter only selected ones
                    .map(CheckBox::getText) // get the brand text
                    .collect(Collectors.toSet()); // collect into a set

            // get selected categories
            Set<String> selectedCategories = categoryCheckboxBox.getChildren().stream()
                    .filter(node -> node instanceof CheckBox)
                    .map(node -> (CheckBox) node)
                    .filter(CheckBox::isSelected)
                    .map(CheckBox::getText)
                    .collect(Collectors.toSet());

            double minPrice = minPriceSlider.getValue();
            double maxPrice = maxPriceSlider.getValue();

            // clear and reload cards with filters applied
            productGrid.getChildren().clear();

            ProductService.getInstance().searchByKeyword("").traverse(product -> {
                boolean brandMatch = selectedBrands.isEmpty() || selectedBrands.contains(product.getBrand());
                boolean categoryMatch = selectedCategories.isEmpty()
                        || selectedCategories.contains(product.getCategory());
                boolean priceMatch = product.getPrice() >= minPrice && product.getPrice() <= maxPrice;

                if (brandMatch && categoryMatch && priceMatch) {
                    buildProductCard(product);
                }
            });
        } catch (Exception e) {
            System.err.println("Error filtering products in Catalog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Handles refreshing the products
    @FXML
    private void handleRefresh() {
        productService.reloadProducts();
        loadProducts();
        loadFilters();
    }
}
