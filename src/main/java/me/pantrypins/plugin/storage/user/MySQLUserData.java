package me.pantrypins.plugin.storage.user;

import me.pantrypins.plugin.*;
import org.bukkit.*;
import org.bukkit.entity.*;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class MySQLUserData {

    public boolean exists(Player player) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        String query = "SELECT * FROM users WHERE (UUID=?)";

        try {
            connection = PantryPins.getMysql().getConnection();

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, player.getUniqueId().toString());
            resultSet = preparedStatement.executeQuery();

            return resultSet.next();
        } catch (SQLException e) {
            PantryPins.getInstance().getLogger().log(Level.SEVERE, "An error occurred while checking if player exists:", e);
        } finally {
            PantryPins.getMysql().closeConnections(preparedStatement, connection, resultSet);
        }

        return false;
    }

    public void createPlayer(Player player) {
        if (exists(player)) {
            return;
        }

        String defaultTag = PantryPins.getInstance().getConfig().getString("settings.default-tag");

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String query = "INSERT INTO users (Name, UUID, Active) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE Name = ?, Active = ?";

        try {
            connection = PantryPins.getMysql().getConnection();

            preparedStatement = connection.prepareStatement(query);

            // Set parameters
            preparedStatement.setString(1, player.getName());
            preparedStatement.setString(2, player.getUniqueId().toString());
            preparedStatement.setString(3, defaultTag);
            preparedStatement.setString(4, player.getName());
            preparedStatement.setString(5, defaultTag);

            // Execute the update
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 0) {
                // No rows were affected, handle this scenario if needed
            }

            if (PantryPins.getInstance().isDataCache()) {
                PantryPins.getInstance().getDataCache().removeFromCache(player.getUniqueId().toString());
                PantryPins.getInstance().getDataCache().cacheData(player.getUniqueId().toString(), defaultTag);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            PantryPins.getMysql().closeConnections(preparedStatement, connection, null);
        }
    }



    public static void setActive(OfflinePlayer player, String identifier) {
        if (PantryPins.getInstance().isDataCache()) {
            String cachedData = PantryPins.getInstance().getDataCache().getCachedData(player.getUniqueId().toString());

            if (cachedData != null && cachedData.equalsIgnoreCase(identifier)) {
                return;
            }
        } else {
            String cachedData = PantryPins.getInstance().getDataCache().getCachedData(player.getUniqueId().toString());

            if (cachedData != null && cachedData.equalsIgnoreCase(identifier)) {
                return;
            }

            PantryPins.getInstance().getDataCache().removeFromCache(player.getUniqueId().toString());
            PantryPins.getInstance().getDataCache().cacheData(player.getUniqueId().toString(), identifier);
            return;
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String query = "INSERT INTO users (Name, UUID, Active) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE Name = ?, Active = ?";

        try {
            connection = PantryPins.getMysql().getConnection();

            preparedStatement = connection.prepareStatement(query);

            // Set parameters
            preparedStatement.setString(1, player.getName());
            preparedStatement.setString(2, player.getUniqueId().toString());
            preparedStatement.setString(3, identifier);
            preparedStatement.setString(4, player.getName());
            preparedStatement.setString(5, identifier);

            // Execute the update
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 0) {
                // No rows were affected, handle this scenario if needed
            }

            PantryPins.getInstance().getDataCache().removeFromCache(player.getUniqueId().toString());
            PantryPins.getInstance().getDataCache().cacheData(player.getUniqueId().toString(), identifier);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            PantryPins.getMysql().closeConnections(preparedStatement, connection, null);
        }
    }

    public static void setActiveManual(OfflinePlayer player, String identifier) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String query = "INSERT INTO users (Name, UUID, Active) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE Name = ?, Active = ?";

        try {
            connection = PantryPins.getMysql().getConnection();

            preparedStatement = connection.prepareStatement(query);

            // Set parameters
            preparedStatement.setString(1, player.getName());
            preparedStatement.setString(2, player.getUniqueId().toString());
            preparedStatement.setString(3, identifier);
            preparedStatement.setString(4, player.getName());
            preparedStatement.setString(5, identifier);

            // Execute the update
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 0) {
                // No rows were affected, handle this scenario if needed
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            PantryPins.getMysql().closeConnections(preparedStatement, connection, null);
        }
    }

    public static String getActive(UUID uuid) {
        if (PantryPins.getInstance().isDataCache()) {
            String cachedData = PantryPins.getInstance().getDataCache().getCachedData(uuid.toString());

            if (cachedData != null) {
                return cachedData;
            }
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        String query = "SELECT Active FROM users WHERE UUID=?";
        String value = "";

        try {
            connection = PantryPins.getMysql().getConnection();

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, uuid.toString());
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                value = resultSet.getString("Active");

                if (PantryPins.getInstance().isDataCache()) {
                    PantryPins.getInstance().getDataCache().cacheData(uuid.toString(), value);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            PantryPins.getMysql().closeConnections(preparedStatement, connection, resultSet);
        }

        return value;
    }
}