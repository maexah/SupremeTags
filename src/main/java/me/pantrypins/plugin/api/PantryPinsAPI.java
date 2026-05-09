package me.pantrypins.plugin.api;

import me.pantrypins.plugin.PantryPins;
import me.pantrypins.plugin.handlers.Tag;
import me.pantrypins.plugin.storage.UserData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PantryPinsAPI {

    /**
     * Returns tag from identifier.
     * @param identifier
     * @return
     */
    public Tag getTag(String identifier) {
        return PantryPins.getInstance().getTagManager().getTag(identifier);
    }

    /**
     * Returns the players tag.
     * @param uuid
     * @return
     */
    public Tag getPlayerTag(UUID uuid) {
        return PantryPins.getInstance().getTagManager().getTag(UserData.getActive(uuid));
    }

    /**
     * Return weather or not if the player has a tag.
     * @param uuid
     * @return
     */
    public boolean hasTag(UUID uuid) {
        return !UserData.getActive(uuid).equalsIgnoreCase("none");
    }

    /**
     * Get all registered tags.
     * @return
     */
    public List<Tag> getAllTags() {
        return new ArrayList<>(PantryPins.getInstance().getTagManager().getTags().values());
    }
}