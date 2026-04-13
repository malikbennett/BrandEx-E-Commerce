package com.brandex.database;

// This class manages the database connection and provides utility methods for executing queries.
// NOTE: We instantiate the PostgreSQL Driver directly because JPMS (the module system) prevents
// DriverManager's ServiceLoader from discovering automatic-module drivers like org.postgresql.jdbc.
import java.sql.*;
import java.util.Properties;
import org.postgresql.Driver;
import com.brandex.utilities.ConfigLoader;

// This class was created with the assistance of an AI language model.
// It manages the database connection and provides utility methods for executing queries.
public class JDBC {
    private static Connection connection;
    private static final String URL = ConfigLoader.get("db.URL");
    private static final String USER = ConfigLoader.get("db.USER");
    private static final String PASSWORD = ConfigLoader.get("db.PASSWORD");

    // Returns a connection to the database.
    public static Connection getConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
        } catch (SQLException ignore) {
        }

        int maxRetries = 3;
        int attempt = 0;
        SQLException lastException = null;

        while (attempt < maxRetries) {
            try {
                Driver driver = new org.postgresql.Driver();
                Properties props = new Properties();
                props.setProperty("user", USER);
                props.setProperty("password", PASSWORD);
                props.setProperty("ssl", "true");
                props.setProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory");

                // Critical stability settings
                props.setProperty("connectTimeout", "5"); // 5 seconds
                props.setProperty("socketTimeout", "10"); // 10 seconds

                connection = driver.connect(URL, props);
                if (connection == null) {
                    throw new SQLException("Driver returned null — check the JDBC URL");
                }
                return connection;
            } catch (SQLException e) {
                attempt++;
                lastException = e;
                System.err.println("Database connection attempt " + attempt + " failed: " + e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
        throw new DatabaseException("Failed to connect to the database after " + maxRetries + " attempts.",
                lastException);
    }

    // Executes a query and returns the result set.
    public static ResultSet query(String sql, Object... params) {
        try {
            PreparedStatement stmt = getConnection().prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            return stmt.executeQuery();
        } catch (SQLException e) {
            throw new DatabaseException("Database query failed: " + sql, e);
        }
    }

    // Used for INSERT, UPDATE, DELETE statements
    public static void execute(String sql, Object... params) {
        try {
            PreparedStatement stmt = getConnection().prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Database execution failed: " + sql, e);
        }
    }
}
