package com.brandex.database;

// This class manages the database connection and provides utility methods for executing queries.
// NOTE: We instantiate the PostgreSQL Driver directly because JPMS (the module system) prevents
// DriverManager's ServiceLoader from discovering automatic-module drivers like org.postgresql.jdbc.
import java.sql.*;
import java.util.Properties;
import org.postgresql.Driver;
import com.brandex.utilities.ConfigLoader;


public class JDBC {
    private static Connection connection;
    private static final String URL = ConfigLoader.get("db.URL");
    private static final String USER = ConfigLoader.get("db.USER");
    private static final String PASSWORD = ConfigLoader.get("db.PASSWORD");

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Driver driver = new org.postgresql.Driver();
                Properties props = new Properties();
                props.setProperty("user", USER);
                props.setProperty("password", PASSWORD);
                props.setProperty("ssl", "true");
                props.setProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory");
                connection = driver.connect(URL, props);
                if (connection == null) {
                    throw new SQLException("Driver returned null — check the JDBC URL");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw e;
            }
        }
        return connection;
    }

    public static ResultSet query(String sql, Object... params) throws SQLException {
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt.executeQuery();
    }

    // Use this for INSERT, UPDATE, DELETE statements
    public static void execute(String sql, Object... params) throws SQLException {
        PreparedStatement stmt = getConnection().prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        stmt.executeUpdate();
    }
}
