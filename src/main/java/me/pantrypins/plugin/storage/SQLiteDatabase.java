package me.pantrypins.plugin.storage;

import me.pantrypins.plugin.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLiteDatabase {

    protected final String ConnectionURL;
    protected Connection connection;

    public SQLiteDatabase(String ConnectionURL) {
        this.ConnectionURL = ConnectionURL;

        this.initialiseDatabase();
    }

    public Connection getConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(ConnectionURL);
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
            PantryPins.getInstance().getLogger().info("------------------------------");
            PantryPins.getInstance().getLogger().info("SQLite: Something wrong with connecting to SQLite database for PantryPins, contact the developer if you see this.");
            PantryPins.getInstance().getLogger().info("------------------------------");
        }

        return connection;
    }

    public void initialiseDatabase() {
        PreparedStatement preparedStatement;

        String userTable = "CREATE TABLE IF NOT EXISTS `users` (Name TEXT NOT NULL, UUID TEXT NOT NULL, Active TEXT NOT NULL, PRIMARY KEY (UUID))";

        try {
            preparedStatement = getConnection().prepareStatement(userTable);
            preparedStatement.executeUpdate();

            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getConnectionURL() {
        return ConnectionURL;
    }
}