package me.pantrypins.plugin.importer;

import me.pantrypins.plugin.PantryPins;
import org.bukkit.command.CommandSender;

import java.io.File;

public interface TagImporter {

    /**
     * The display name of the tag plugin (e.g., "DeluxeTags").
     */
    String getPluginName();

    /**
     * The configuration file of the tag plugin.
     */
    File getConfigFile();

    /**
     * Imports tags into PantryPins.
     *
     * @param plugin PantryPins instance
     * @param sender Command sender for messages
     * @param force Whether to override existing tags
     */
    void importTags(PantryPins plugin, CommandSender sender, boolean force);
}