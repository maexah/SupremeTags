package me.pantrypins.plugin.storage;

import me.pantrypins.plugin.*;
import me.pantrypins.plugin.storage.user.H2UserData;
import me.pantrypins.plugin.storage.user.MySQLUserData;
import me.pantrypins.plugin.storage.user.SQLiteUserData;
import org.bukkit.*;
import org.bukkit.entity.*;

import java.sql.SQLException;
import java.util.*;

public class UserData {

    public static void createPlayer(Player player) {
        if (PantryPins.getInstance().isH2()) {
            PantryPins.getInstance().getUserData().createPlayer(player);
        } else if (PantryPins.getInstance().isMySQL() || PantryPins.getInstance().isMaria()) {
            PantryPins.getInstance().getUser().createPlayer(player);
        } else if (PantryPins.getInstance().isSQLite()) {
            PantryPins.getInstance().getSQLiteUser().createPlayer(player);
        }
    }

    public static void setActive(OfflinePlayer player, String identifier) {
        if (PantryPins.getInstance().isH2()) {
            H2UserData.setActive(player, identifier);
        } else if (PantryPins.getInstance().isMySQL() || PantryPins.getInstance().isMaria()) {
            MySQLUserData.setActive(player, identifier);
        } else if (PantryPins.getInstance().isSQLite()) {
            SQLiteUserData.setActive(player, identifier);
        }
    }

    public static void setActiveManual(OfflinePlayer player, String identifier) {
        if (PantryPins.getInstance().isMySQL() || PantryPins.getInstance().isMaria()) {
            MySQLUserData.setActiveManual(player, identifier);
        }
    }

    public static boolean isConnected() {
        if (PantryPins.getInstance().isH2()) {
            try {
                if (!PantryPins.getH2Database().getConnection().isClosed()) {
                    return true;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else if (PantryPins.getInstance().isMySQL() || PantryPins.getInstance().isMaria()) {
            try {
                if (!PantryPins.getMysql().getConnection().isClosed()) {
                    return true;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else if (PantryPins.getInstance().isSQLite()) {
            try {
                if (!PantryPins.getSQLite().getConnection().isClosed()) {
                    return true;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return false;
    }

    public static String getActive(UUID uuid) {

        if (PantryPins.getInstance().isH2()) {
            return H2UserData.getActive(uuid);
        } else if (PantryPins.getInstance().isMySQL() || PantryPins.getInstance().isMaria()) {
            return MySQLUserData.getActive(uuid);
        } else if (PantryPins.getInstance().isSQLite()) {
            return SQLiteUserData.getActive(uuid);
        }

        return "";
    }
}
