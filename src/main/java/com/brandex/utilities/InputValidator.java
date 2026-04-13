package com.brandex.utilities;

// This utility class was created using the assistance of an AI language model.
// It provides basic input validation functionality.
public class InputValidator {
    // Checks if an email is valid.
    public static boolean isValidEmail(String email) {
        String pattern = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";
        return email != null && email.matches(pattern);
    }
}
