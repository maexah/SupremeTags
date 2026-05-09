package me.pantrypins.plugin.importer.types;

import me.pantrypins.plugin.PantryPins;
import me.pantrypins.plugin.importer.TagImporter;
import me.pantrypins.plugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FreeSupremeTagsImporter implements TagImporter {

    @Override
    public String getPluginName() {
        return "Free PantryPins";
    }

    @Override
    public File getConfigFile() {
        return new File(Bukkit.getWorldContainer(), "plugins/PantryPins/free-tags.yml");
    }

    @Override
    public void importTags(PantryPins plugin, CommandSender sender, boolean force) {
        File file = getConfigFile();

        if (sender == null) sender = Bukkit.getConsoleSender();

        if (!file.exists()) {
            //Utils.msgPlayer(sender, "&6Merger: &7Missing 'free-tags.yml' in PantryPins folder.");
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("tags");

        if (section == null) {
            Utils.msgPlayer(sender, "&6Merger: &7Invalid structure in 'free-tags.yml'.");
            return;
        }

        section.getKeys(false).forEach(id -> {
            if (!force && plugin.getTagManager().getTags().containsKey(id)) return;

            String tag = config.getString("tags." + id + ".tag");
            String description = config.getString("tags." + id + ".description", "N/A");
            String permission = config.getString("tags." + id + ".permission");
            String material = config.getString("tags." + id + ".icon.material", "NAME_TAG");

            List<String> desc = new ArrayList<>();
            desc.add(description);

            plugin.getTagManager().createTag(id, material, tag, desc, permission, 100);
        });

        Utils.msgPlayer(sender, "&6Merger: &7Imported tags from &6Free PantryPins&7.");
    }
}