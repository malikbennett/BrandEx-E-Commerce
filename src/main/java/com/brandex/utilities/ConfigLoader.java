package com.brandex.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


// This utility class was created using the assistance of an AI language model, and is intended for educational purposes. It loads configuration properties from a the config.properties file and provides a method to access them.
public class ConfigLoader {
    private static Properties props = new Properties();

    static {
        try (InputStream input = ConfigLoader.class.getResourceAsStream("/com/brandex/config.properties")) {
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Could not load config.properties", e);
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
}
