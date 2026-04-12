package com.brandex;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import atlantafx.base.theme.CupertinoDark;
// import atlantafx.base.theme.CupertinoLight;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private static StackPane root;

    // This is the main entry point for our JavaFX application. It loads the main
    // FXML file and sets up the stage.
    @Override
    public void start(Stage stage) throws IOException {
        Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());

        stage.setMaximized(true);
        stage.setMinWidth(640);
        stage.setMinHeight(360);
        stage.setTitle("BrandEx Online Store");
        stage.getIcons().add(new Image(App.class.getResourceAsStream("/com/brandex/images/icons/logo.png")));
        scene = setScene();
        stage.setScene(scene);
        stage.show();
    }

    public static Scene setScene() {
        root = setRoot("main");
        Scene scene = new Scene(root);
        scene.getStylesheets().add(App.class.getResource("/com/brandex/style.css").toExternalForm());
        return scene;
    }

    public static StackPane getRoot() {
        return root;
    }

    // This loads in our FXML files and updates the scene root to navigate between
    // screens
    public static StackPane setRoot(String fxml) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/brandex/fxml/" + fxml + ".fxml"));
            root = fxmlLoader.load();
            if (scene != null)
                scene.setRoot(root); // Update the live scene to show the new screen
            return root;
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxml + ".fxml");
            e.printStackTrace();
        }
        return null;
    }

    // Our main method just launches the JavaFX application
    public static void main(String[] args) {
        launch(args);
    }

}
