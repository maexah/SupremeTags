package me.pantrypins.plugin.managers;

import me.pantrypins.plugin.PantryPins;
import me.pantrypins.plugin.importer.TagImporter;
import me.pantrypins.plugin.importer.types.AlonsoTagsImporter;
import me.pantrypins.plugin.importer.types.DeluxeTagsImporter;
import me.pantrypins.plugin.importer.types.EternalTagsImporter;
import me.pantrypins.plugin.importer.types.FreeSupremeTagsImporter;
import me.pantrypins.plugin.utils.Utils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class MergeManager {

    private final PantryPins plugin;
    private final List<TagImporter> importers = new ArrayList<>();

    public MergeManager(PantryPins plugin) {
        this.plugin = plugin;
        registerImporters();
    }

    private void registerImporters() {
        importers.add(new DeluxeTagsImporter());
        importers.add(new EternalTagsImporter());
        importers.add(new AlonsoTagsImporter());
        importers.add(new FreeSupremeTagsImporter());
    }

    public void merge(CommandSender sender, boolean force) {
        boolean autoMerge = plugin.getConfig().getBoolean("settings.auto-merge");

        if (!autoMerge && !force) {
            Utils.msgPlayer(sender, "&6Merger: &7Auto-merge is disabled in config.yml.");
            return;
        }

        for (TagImporter importer : importers) {
            try {
                importer.importTags(plugin, sender, force);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to import from " + importer.getPluginName() + ": " + e.getMessage());
            }
        }
    }
}