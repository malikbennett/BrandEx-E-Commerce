package com.brandex.utilities;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.InputStream;

// Utility class for loading images with automatic fallback handling.
// class was created with the assistance of an AI language model.
public class ImageLoader {

    private static final String DEFAULT_IMAGE_PATH = "/com/brandex/images/no_image.png";

    // Loads an image into an ImageView.
    public static void load(ImageView imageView, String path) {
        if (path == null || path.trim().isEmpty()) {
            loadDefaultImage(imageView);
            return;
        }
        try {
            if (path.startsWith("http://") || path.startsWith("https://")) {
                Image image = new Image(path, true);

                image.errorProperty().addListener((obs, oldVal, errorOccurred) -> {
                    if (errorOccurred) {
                        loadDefaultImage(imageView);
                    }
                });

                imageView.setImage(image);
            } else {
                InputStream is = ImageLoader.class.getResourceAsStream(path);
                if (is == null)
                    throw new Exception("Resource not found: " + path);
                imageView.setImage(new Image(is));
            }
        } catch (Exception e) {
            loadDefaultImage(imageView);
        }
    }

    // Loads a product image into an ImageView.
    public static void loadProductImage(ImageView imageView, String imageName) {
        if (imageName == null || imageName.trim().isEmpty()) {
            loadDefaultImage(imageView);
            return;
        }
        String fullUrl = ConfigLoader.get("db.PRODUCT_IMAGE_PATH") + imageName;
        load(imageView, fullUrl);
    }

    // Loads the default image into an ImageView.
    public static void loadDefaultImage(ImageView imageView) {
        try {
            InputStream is = ImageLoader.class.getResourceAsStream(DEFAULT_IMAGE_PATH);
            if (is != null) {
                imageView.setImage(new Image(is));
            }
        } catch (Exception e) {
            System.err.println("Critical: Default image not found at " + DEFAULT_IMAGE_PATH);
        }
    }
}
