package com.brandex.utilities;

import java.security.SecureRandom;

public class OTPGenerator {
    public static String generate() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000); // always 6 digits
        return String.valueOf(otp);
    }
}
