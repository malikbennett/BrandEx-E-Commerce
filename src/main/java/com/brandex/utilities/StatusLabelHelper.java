package com.brandex.utilities;

import javafx.scene.control.Label;

// Utility class to manage UI status messages with consistent styling for success and error states.
// class was created with the assistance of an AI language model.
public class StatusLabelHelper {

    private static final String COLOR_ERROR = "#f44336";
    private static final String COLOR_SUCCESS = "#4caf50";

    // Sets a message on a label with dynamic styling.
    public static void setStatus(Label label, String message, boolean isError) {
        if (label == null)
            return;
        label.setText(message);
        label.setStyle("-fx-text-fill: " + (isError ? COLOR_ERROR : COLOR_SUCCESS) + ";");
        label.setVisible(true);
        label.setManaged(true);
    }

    // Displays an error message in red.
    public static void showError(Label label, String message) {
        setStatus(label, message, true);
    }

    // Displays a success message in green.
    public static void showSuccess(Label label, String message) {
        setStatus(label, message, false);
    }

    // Clears the message and hides the label.
    public static void clear(Label label) {
        if (label == null)
            return;
        label.setText("");
        label.setVisible(false);
        label.setManaged(false);
    }
}
