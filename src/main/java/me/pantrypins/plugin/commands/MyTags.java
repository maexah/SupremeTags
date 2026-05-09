package me.pantrypins.plugin.commands;

import me.pantrypins.plugin.PantryPins;
import me.pantrypins.plugin.enums.TPermissions;
import me.pantrypins.plugin.guis.personaltags.PersonalTagsMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Objects;

import static me.pantrypins.plugin.utils.Utils.msgPlayer;

public class MyTags implements CommandExecutor {

    private final FileConfiguration messages =
            PantryPins.getInstance().getConfigManager()
                    .getConfig("messages.yml").get();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by players.");
            return true;
        }

        Player player = (Player) sender;

        // Permission check
        if (!player.hasPermission(TPermissions.MYTAGS)) {
            msgPlayer(player, messages.getString("messages.no-permission")
                    .replace("%prefix%", messages.getString("messages.prefix")));
            return true;
        }

        // Disabled message
        String disabledMsg = messages
                .getString("messages.ptags-disabled")
                .replace("%prefix%", Objects.requireNonNull(messages.getString("messages.prefix")));

        // If personal tags disabled
        if (!PantryPins.getInstance().getConfig().getBoolean("settings.personal-tags.enable")) {
            msgPlayer(player, disabledMsg);
            return true;
        }

        // Open menu
        new PersonalTagsMenu(PantryPins.getMenuUtil(player)).open();
        return true;
    }
}