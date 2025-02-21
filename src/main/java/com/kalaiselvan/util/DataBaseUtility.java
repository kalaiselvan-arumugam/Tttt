package com.kalaiselvan.util;

import org.apache.commons.dbcp2.BasicDataSource; import com.google.gson.JsonObject; import java.sql.Connection; import java.sql.CallableStatement; import java.sql.ResultSet; import java.sql.SQLException;

public class DataBaseUtility { private static BasicDataSource staticDataSource; private static boolean isFailoverActive = false;

/**
 * Get the current data source. If not initialized, attempts to connect to the main DB first,
 * then the failover DB if necessary.
 */
public static BasicDataSource getDataSource() {
    if (staticDataSource == null) {
        if (!initializeDataSource("main")) {
            System.err.println("Failed to connect to the main database. Attempting failover...");
            if (initializeDataSource("failover")) {
                isFailoverActive = true;
                System.out.println("Connected to the failover database.");
            } else {
                System.err.println("Failed to connect to the failover database as well.");
            }
        } else {
            System.out.println("Connected to the main database.");
        }
    }
    return staticDataSource;
}

/**
 * Initializes the data source with the specified configuration type ("main" or "failover").
 * Includes robust connection validation.
 */
private static boolean initializeDataSource(String dbType) {
    try {
        BasicDataSource basicDataSource = new BasicDataSource();
        JsonObject databaseConfig = getDatabaseConfig(dbType);

        basicDataSource.setUrl(String.format("jdbc:postgresql://%s:%s/%s",
                databaseConfig.get("ip").getAsString(),
                databaseConfig.get("port").getAsString(),
                databaseConfig.get("database").getAsString()));
        basicDataSource.setUsername(databaseConfig.get("userName").getAsString());
        basicDataSource.setPassword(databaseConfig.get("password").getAsString());
        basicDataSource.setMinIdle(databaseConfig.get("minIdleConnection").getAsInt());
        basicDataSource.setMaxIdle(databaseConfig.get("maxIdleConnection").getAsInt());
        basicDataSource.setMaxOpenPreparedStatements(100);

        // Validate the connection by executing a test query
        try (Connection connection = basicDataSource.getConnection()) {
            if (connection != null && !connection.isClosed() && connection.isValid(5)) {
                closeDataSource();
                staticDataSource = basicDataSource;
                isFailoverActive = dbType.equals("failover");
                return true;
            } else {
                System.err.println("Connection validation failed for " + dbType + " database.");
            }
        }
    } catch (Exception ex) {
        System.err.println("Exception while connecting to the " + dbType + " database:");
        ex.printStackTrace();
    }
    return false;
}

/**
 * Provides hardcoded database configurations for main and failover databases.
 */
private static JsonObject getDatabaseConfig(String dbType) {
    JsonObject config = new JsonObject();
    if ("main".equals(dbType)) {
        config.addProperty("ip", "localhost");
        config.addProperty("port", "5432");
        config.addProperty("database", "main_db");
        config.addProperty("userName", "main_user");
        config.addProperty("password", "main_password");
        config.addProperty("minIdleConnection", 5);
        config.addProperty("maxIdleConnection", 10);
    } else if ("failover".equals(dbType)) {
        config.addProperty("ip", "localhost");
        config.addProperty("port", "5433");
        config.addProperty("database", "failover_db");
        config.addProperty("userName", "failover_user");
        config.addProperty("password", "failover_password");
        config.addProperty("minIdleConnection", 2);
        config.addProperty("maxIdleConnection", 5);
    }
    return config;
}

/**
 * Closes the current data source and releases all connections.
 */
public static void closeDataSource() {
    try {
        if (staticDataSource != null) {
            staticDataSource.close();
            staticDataSource = null;
            System.out.println("Data source closed.");
        }
    } catch (Exception ex) {
        System.err.println("Exception while closing the data source:");
        ex.printStackTrace();
    }
}

/**
 * Updates the database connection details and recreates the connection pool automatically.
 */
public static void updateDataSource() {
    System.out.println("Updating database configuration...");
    closeDataSource();
    getDataSource();
}

public static boolean isFailoverActive() {
    return isFailoverActive;
}

/**
 * Executes a stored procedure with the given name and parameters.
 */
public static ResultSet executeStoredProcedure(String procedureName, Object... params) {
    try (Connection connection = getDataSource().getConnection();
         CallableStatement stmt = connection.prepareCall("{call " + procedureName + "}");) {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt.executeQuery();
    } catch (SQLException e) {
        System.err.println("Error executing stored procedure: " + e.getMessage());
        e.printStackTrace();
    }
    return null;
}

}
