package com.brandex.utilities;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

// Utility class for loading images with automatic fallback handling.
// class was created with the assistance of an AI language model.
public class ImageLoader {

    private static final String DEFAULT_IMAGE_PATH = "/com/brandex/images/no_image.png";
    private static final Map<String, Image> imageCache = new HashMap<>();
    private static Image defaultImage;

    // Loads an image into an ImageView.
    public static void load(ImageView imageView, String path) {
        if (path == null || path.trim().isEmpty()) {
            loadDefaultImage(imageView);
            return;
        }

        // Check cache first to avoid redundant hits
        if (imageCache.containsKey(path)) {
            imageView.setImage(imageCache.get(path));
            return;
        }

        try {
            if (path.startsWith("http://") || path.startsWith("https://")) {
                // Background loading set to true for network images
                Image image = new Image(path, true);

                image.errorProperty().addListener((obs, oldVal, errorOccurred) -> {
                    if (errorOccurred) {
                        // If loading fails, remove from cache so we can try again later
                        imageCache.remove(path);
                        loadDefaultImage(imageView);
                    }
                });

                // Store in cache (the image will populate in background)
                imageCache.put(path, image);
                imageView.setImage(image);
            } else {
                InputStream is = ImageLoader.class.getResourceAsStream(path);
                if (is == null)
                    throw new Exception("Resource not found: " + path);
                
                Image image = new Image(is);
                imageCache.put(path, image);
                imageView.setImage(image);
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
            if (defaultImage == null) {
                InputStream is = ImageLoader.class.getResourceAsStream(DEFAULT_IMAGE_PATH);
                if (is != null) {
                    defaultImage = new Image(is);
                }
            }
            if (defaultImage != null) {
                imageView.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.err.println("Critical: Default image not found at " + DEFAULT_IMAGE_PATH);
        }
    }
}
