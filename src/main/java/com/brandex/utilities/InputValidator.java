package com.brandex.utilities;

public class InputValidator {
    public static boolean isValidEmail(String email) {
        String pattern = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";
        return email != null && email.matches(pattern);
    }
}
