package com.brandex.utilities;

import javafx.scene.control.Label;

public class ThrowError {
    public static void errorLabel(Label label, String message) {
        label.setText(message);
    }
    public static void clearError(Label label) {
        label.setText("");
    }
}
