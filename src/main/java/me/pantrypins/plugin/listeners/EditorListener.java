package me.pantrypins.plugin.listeners;

import me.pantrypins.plugin.PantryPins;
import me.pantrypins.plugin.enums.EditingType;
import me.pantrypins.plugin.guis.personaltags.PersonalTagEditorMenu;
import me.pantrypins.plugin.guis.tageditor.SpecificTagMenu;
import me.pantrypins.plugin.handlers.Editor;
import me.pantrypins.plugin.handlers.Tag;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static me.pantrypins.plugin.utils.Utils.deformat;
import static me.pantrypins.plugin.utils.Utils.msgPlayer;

public class EditorListener implements Listener {

    private FileConfiguration messages = PantryPins.getInstance().getConfigManager().getConfig("messages.yml").get();

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent e) {
        edit(e);
    }

    public void edit(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        if (!PantryPins.getInstance().getEditorList().containsKey(player)) return;

        String message = e.getMessage();
        String deformat_message = deformat(message);
        Editor editor = PantryPins.getInstance().getEditorList().get(player);
        EditingType type = editor.getType();

        e.setCancelled(true);

        // === Handle cancel input ===
        if (deformat_message.equalsIgnoreCase("cancel")) {
            PantryPins.getInstance().getEditorList().remove(player);

            // Return to correct menu depending on edit type
            if (!editor.isPersonalEdit()) {
                runTaskLater(() -> new SpecificTagMenu(
                        PantryPins.getMenuUtilIdentifier(player, editor.getIdentifier())).open(), 1L);
            } else {
                runTaskLater(() -> new PersonalTagEditorMenu(
                        PantryPins.getMenuUtilIdentifier(player, editor.getIdentifier())).open(), 1L);
            }

            String cancelled = messages.getString("messages.editor.cancelled", "%prefix% &7edit cancelled, no changes have taken place.")
                    .replace("%prefix%", Objects.requireNonNull(messages.getString("messages.prefix")));
            msgPlayer(player, cancelled);
            return;
        } else {

            // === Continue normal editing ===
            if (!editor.isPersonalEdit()) {
                Tag tag = PantryPins.getInstance().getTagManager().getTag(editor.getIdentifier());
                switch (type) {
                    case CHANGING_TAG:
                        List<String> tagList = tag.getTag();
                        tagList.add(message);
                        tag.setTag(tagList);
                        break;
                    case CHANGING_PERMISSION:
                        tag.setPermission(deformat_message);
                        break;
                    case CHANGING_CATEGORY:
                        tag.setCategory(deformat_message);
                        break;
                    case CHANGING_RARITY:
                        tag.setRarity(deformat_message);
                        break;
                    case CHANGING_COST:
                        tag.getEconomy().setAmount(Double.parseDouble(deformat_message));
                        break;
                    case CHANGING_DESCRIPTION:
                        List<String> desc = tag.getDescription();
                        desc.add(message);
                        tag.setDescription(desc);
                        break;
                    case CHANGING_ORDER:
                        tag.setOrder(Integer.parseInt(deformat_message));
                        break;
                }

                PantryPins.getInstance().getTagManager().saveTag(tag);
                PantryPins.getInstance().getTagManager().unloadTags();
                PantryPins.getInstance().getTagManager().loadTags(true);
                PantryPins.getInstance().getCategoryManager().initCategories();

                runTaskLater(() -> new SpecificTagMenu(
                        PantryPins.getMenuUtilIdentifier(player, editor.getIdentifier())).open(), 1L);

            } else {
                Tag tag = PantryPins.getInstance().getPlayerManager()
                        .getTag(player.getUniqueId(), editor.getIdentifier());
                switch (type) {
                    case CHANGING_TAG:
                        List<String> tagList = new ArrayList<>();
                        tagList.add(message);
                        tag.setTag(tagList);
                        break;
                    case CHANGING_DESCRIPTION:
                        List<String> desc = tag.getDescription();
                        desc.add(message);
                        tag.setDescription(desc);
                        break;
                }

                PantryPins.getInstance().getPlayerManager().save(tag, player);
                runTaskLater(() -> new PersonalTagEditorMenu(
                        PantryPins.getMenuUtilIdentifier(player, editor.getIdentifier())).open(), 1L);
            }

            PantryPins.getInstance().getEditorList().remove(player);

            String tag_updated = messages.getString("messages.tag-updated")
                    .replace("%prefix%", Objects.requireNonNull(messages.getString("messages.prefix")));
            msgPlayer(player, tag_updated);
        }
    }

    /**
     * Runs a task later supporting Folia & Bukkit.
     */
    private void runTaskLater(Runnable task, long delayTicks) {
        Plugin plugin = PantryPins.getInstance();

        if (PantryPins.getInstance().isFoliaFound()) {
            Bukkit.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin,
                    scheduledTask -> task.run(),
                    delayTicks,
                    Long.MAX_VALUE // run once effectively, could cancel immediately after if needed
            );
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }
}