package me.pantrypins.plugin.storage;

import me.pantrypins.plugin.PantryPins;
import me.pantrypins.plugin.handlers.Tag;

import java.sql.SQLException;
import java.util.List;

public class TagData {

    // -------------------------------------------------------
    // CREATE TAG
    // -------------------------------------------------------
    public static void createTag(Tag tag) {
        if (PantryPins.getInstance().isMySQL() || PantryPins.getInstance().isMaria()) {
            PantryPins.getInstance().getMySQLTags().saveTag(tag);
        } else if (PantryPins.getInstance().isSQLite()) {
            PantryPins.getInstance().getSqLiteTags().saveTag(tag);
        }
    }

    // -------------------------------------------------------
    // DELETE TAG
    // -------------------------------------------------------
    public static void deleteTag(String identifier) {
        if (PantryPins.getInstance().isMySQL() || PantryPins.getInstance().isMaria()) {
            PantryPins.getInstance().getSqLiteTags().deleteTag(identifier);
        } else if (PantryPins.getInstance().isSQLite()) {
            PantryPins.getInstance().getSqLiteTags().deleteTag(identifier);
        }
    }

    // -------------------------------------------------------
    // UPDATE TAG
    // -------------------------------------------------------
    public static void updateTag(Tag tag) {
        if (PantryPins.getInstance().isMySQL() || PantryPins.getInstance().isMaria()) {
            PantryPins.getInstance().getSqLiteTags().updateTag(tag);
        } else if (PantryPins.getInstance().isSQLite()) {
            PantryPins.getInstance().getSqLiteTags().updateTag(tag);
        }
    }

    // -------------------------------------------------------
    // GET TAG (ONE)
    // -------------------------------------------------------
    public static Tag getTag(String identifier) {
        //if (PantryPins.getInstance().isMySQL() || PantryPins.getInstance().isMaria()) {
        //    return PantryPins.getInstance().getSqLiteTags().getTag(identifier);
        //} else if (PantryPins.getInstance().isSQLite()) {
        //    return PantryPins.getInstance().getSqLiteTags().getTag(identifier);
        //}

        return null;
    }

    // -------------------------------------------------------
    // GET ALL TAGS
    // -------------------------------------------------------
    public static void getAllTags() {
        if (PantryPins.getInstance().isMySQL() || PantryPins.getInstance().isMaria()) {
            PantryPins.getInstance().getTagManager().setTagsMap(PantryPins.getInstance().getMySQLTags().loadTags());
        } else if (PantryPins.getInstance().isSQLite()) {
            PantryPins.getInstance().getTagManager().setTagsMap(PantryPins.getInstance().getSqLiteTags().loadTags());
        }
    }

    // -------------------------------------------------------
    // IS CONNECTED
    // -------------------------------------------------------
    public static boolean isConnected() {
        try {
            if (PantryPins.getInstance().isMySQL() || PantryPins.getInstance().isMaria()) {
                return !PantryPins.getMysql().getConnection().isClosed();
            } else if (PantryPins.getInstance().isSQLite()) {
                return !PantryPins.getSQLite().getConnection().isClosed();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return false;
    }
}
