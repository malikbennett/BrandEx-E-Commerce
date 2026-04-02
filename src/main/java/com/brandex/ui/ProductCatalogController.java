package com.brandex.ui;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.brandex.models.Product;
import com.brandex.service.ProductService;
import com.brandex.utilities.ImageLoader;

import atlantafx.base.controls.Card;
import atlantafx.base.controls.Tile;
import atlantafx.base.theme.Styles;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

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
    private Label priceLabel;
    @FXML
    private VBox categoryFilterBox;
    @FXML
    private VBox categoryCheckboxBox;
    @FXML
    private VBox brandFilterBox;
    @FXML
    private VBox brandCheckboxBox;

    @FXML
    public void initialize() {
        loadProducts();
        loadFilters();
    }

    private void buildProductCard(Product product) {
        // the card itself, has header, subheader, body, and footer sections
        Card card = new Card();
        card.getStyleClass().add(Styles.ELEVATED_1);
        card.setMinWidth(250);
        card.setMaxWidth(300);
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
        // Rectangle clip = new Rectangle(250, 180);
        // clip.setArcWidth(8);
        // clip.setArcHeight(8);
        // imageContainer.setClip(clip);
        StackPane.setAlignment(imgView, Pos.CENTER);
        card.setHeader(imageContainer); // sets the image as the header of the card
        // title and category for subheader
        Tile title = new Tile(product.getName(), product.getCategory());
        title.setPadding(new Insets(20, 0, 0, 0));
        card.setSubHeader(title); // sets the title and category as the subheader of the card
        // rating and brand for body
        VBox body = new VBox(5);
        Text rating = new Text(
                product.getRating() > 0 ? String.format("%.1f ★", product.getRating()) : "No ratings");
        Text brand = new Text(product.getBrand());
        body.getChildren().add(rating);
        body.getChildren().add(brand);
        card.setBody(body); // sets the rating and brand as the body of the card
        // price and add to cart button for footer
        Tile footer = new Tile(String.format("$%.2f", product.getPrice()), null);
        Button addToCart = new Button("Add");
        addToCart.setOnAction(e -> {
            // Handle add to cart action
        });
        footer.setAction(addToCart);
        card.setFooter(footer); // sets the price and add to cart button as the footer of the card
        productGrid.getChildren().add(card);
    }

    private void loadProducts() {

        productGrid.getChildren().clear();
        ProductService productService = ProductService.getInstance();

        try {
            productService.searchByKeyword("").traverse(product -> {
                buildProductCard(product);
            });
        } catch (Exception e) {
            System.err.println("Error loading products in Catalog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadFilters() {

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

    private void updatePriceLabel() {
        priceLabel.setText(String.format("$%.0f — $%.0f", minPriceSlider.getValue(), maxPriceSlider.getValue()));
    }

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
}
