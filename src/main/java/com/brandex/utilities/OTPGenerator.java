package com.brandex.utilities;

import java.security.SecureRandom;

// This utility class was created using the assistance of an AI language model.
// It provides basic OTP generation functionality.
public class OTPGenerator {
    // Generates a random 6-digit OTP.
    public static String generate() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
