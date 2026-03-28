package com.brandex;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    // This is the main entry point for our JavaFX application. It loads the main FXML file and sets up the stage.
    @Override
    public void start(Stage stage) throws IOException {
        // Calls the setRoot method to load the main.fxml file and get the root node
        Parent root = setRoot("main");
        scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("BrandEx Online Store");
        stage.show();
    }
    // This loads in our FXML files and updates the scene root to navigate between screens
    public static Parent setRoot(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/brandex/fxml/" + fxml + ".fxml"));
        Parent root = fxmlLoader.load();
        if (scene != null) {
            scene.setRoot(root); // Update the live scene to show the new screen
        }
        return root;
    }
    // Our main method just launches the JavaFX application
    public static void main(String[] args) {
        launch();
    }

}
