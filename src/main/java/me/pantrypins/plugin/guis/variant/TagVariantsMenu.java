package me.pantrypins.plugin.guis.variant;

import de.tr7zw.nbtapi.NBTItem;
import me.pantrypins.plugin.PantryPins;
import me.pantrypins.plugin.api.events.TagAssignEvent;
import me.pantrypins.plugin.api.events.TagResetEvent;
import me.pantrypins.plugin.guis.MainMenu;
import me.pantrypins.plugin.guis.TagMenu;
import me.pantrypins.plugin.guis.personaltags.PersonalTagsMenu;
import me.pantrypins.plugin.handlers.Tag;
import me.pantrypins.plugin.handlers.Variant;
import me.pantrypins.plugin.handlers.menu.Menu;
import me.pantrypins.plugin.handlers.menu.MenuUtil;
import me.pantrypins.plugin.handlers.menu.Paged;
import me.pantrypins.plugin.managers.TagManager;
import me.pantrypins.plugin.storage.UserData;
import me.pantrypins.plugin.utils.ItemResolver;
import me.pantrypins.plugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

import static me.pantrypins.plugin.utils.Utils.*;

public class TagVariantsMenu extends Paged {

    private final List<Variant> variants;
    private final Tag tag;
    private final FileConfiguration messages = PantryPins.getInstance().getConfigManager().getConfig("messages.yml").get();
    private final FileConfiguration guis = PantryPins.getInstance().getConfigManager().getConfig("guis.yml").get();

    public TagVariantsMenu(MenuUtil menuUtil, Tag tag) {
        super(menuUtil);
        this.tag = tag;
        this.variants = tag.getVariants();

        enableAutoUpdate(true);
    }

    @Override
    public String getMenuName() {
        String title = guis.getString("gui.tag-variants-menu.title");

        if (tag != null) {
            title = title.replace("%tag%", tag.getTag().get(0));
            title = title.replace("%identifier%", tag.getIdentifier());
            title = globalPlaceholders(menuUtil.getOwner(), title);
        }

        return format(title);
    }

    @Override
    public int getSlots() {
        return guis.getInt("gui.tag-variants-menu.size");
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player)e.getWhoClicked();

        String no_tag_selected = messages.getString("messages.no-tag-selected").replaceAll("%prefix%", Objects.requireNonNull(messages.getString("messages.prefix")));

        if (e.getClickedInventory().getType() == InventoryType.PLAYER) {
            e.setCancelled(true);
            return;
        }

        if (e.getCurrentItem().getType().equals(Material.valueOf(Objects.requireNonNull(this.guis.getString("gui.items.glass.material")).toUpperCase())))
            e.setCancelled(true);

        NBTItem nbt = new NBTItem(e.getCurrentItem());

        if (nbt.hasTag("isVariant") && nbt.hasTag("variant_identifier")) {
            if (nbt.getBoolean("isVariant").booleanValue()) {
                String var_identifier = nbt.getString("variant_identifier");
                Variant var = this.tag.getVariant(var_identifier);
                if (player.hasPermission(var.getPermission()) &&
                        !UserData.getActive(player.getUniqueId()).equalsIgnoreCase(var_identifier) && var_identifier != null) {

                    TagAssignEvent tagevent = new TagAssignEvent(player, var_identifier, false);
                    Bukkit.getPluginManager().callEvent(tagevent);
                    if (tagevent.isCancelled())
                        return;

                    UserData.setActive(player, tagevent.getTag());
                    open();

                    this.menuUtil.setIdentifier(tagevent.getTag());
                    if (PantryPins.getInstance().getConfig().getBoolean("settings.gui-messages")) {
                        String select = this.messages.getString("messages.tag-select-message").replaceAll("%prefix%", Objects.requireNonNull(this.messages.getString("messages.prefix")));
                        select = replacePlaceholders(this.menuUtil.getOwner(), select);
                        msgPlayer(player, select.replace("%identifier%", var_identifier).replaceAll("%tag%", var.getTag().get(0)));
                    }
                } else if (player.hasPermission(var.getPermission()) &&
                        UserData.getActive(player.getUniqueId()).equalsIgnoreCase(var_identifier) && var_identifier != null) {
                    TagResetEvent tagEvent = new TagResetEvent(player, false);
                    Bukkit.getPluginManager().callEvent(tagEvent);

                    if (tagEvent.isCancelled()) return;

                    String defaultTag = PantryPins.getInstance().getConfig().getString("settings.default-tag");

                    UserData.setActive(player, defaultTag);
                    super.refresh();
                    menuUtil.setIdentifier(defaultTag);

                    if (PantryPins.getInstance().getConfig().getBoolean("settings.gui-messages")) {
                        msgPlayer(player, messages.getString("messages.reset-message").replaceAll("%prefix%", Objects.requireNonNull(messages.getString("messages.prefix"))));
                    }
                }
            }
        }

        if (nbt.hasTag("name")) {
            String name = nbt.getString("name");

            if (name.equalsIgnoreCase("close")) {
                player.closeInventory();
            }

            if (name.equalsIgnoreCase("personal-tags")) {
                new PersonalTagsMenu(PantryPins.getMenuUtil(player)).open();
            }

            if (name.equalsIgnoreCase("search")) {
                player.closeInventory();
                openSearchContainer(player);
            }

            if (name.equalsIgnoreCase("reset")) {
                if (menuUtil.getIdentifier() == null || (menuUtil.getIdentifier().equalsIgnoreCase("none"))) {
                    msgPlayer(player, no_tag_selected);
                    return;
                }

                if (!PantryPins.getInstance().getConfig().getBoolean("settings.forced-tag")) {
                    TagResetEvent tagEvent = new TagResetEvent(player, false);
                    Bukkit.getPluginManager().callEvent(tagEvent);

                    if (tagEvent.isCancelled()) return;

                    if (menuUtil.getIdentifier() != null || !(menuUtil.getIdentifier().equalsIgnoreCase("none"))) {
                        Tag t = PantryPins.getInstance().getTagManager().getTag(menuUtil.getIdentifier());
                        if (t != null) {
                            t.removeEffects(menuUtil.getOwner());
                        }
                    }

                    UserData.setActive(player, "None");
                    super.refresh();
                    menuUtil.setIdentifier("None");

                    if (PantryPins.getInstance().getConfig().getBoolean("settings.gui-messages")) {
                        msgPlayer(player, messages.getString("messages.reset-message").replaceAll("%prefix%", Objects.requireNonNull(messages.getString("messages.prefix"))));
                    }
                } else {
                    TagResetEvent tagEvent = new TagResetEvent(player, false);
                    Bukkit.getPluginManager().callEvent(tagEvent);

                    if (tagEvent.isCancelled()) return;

                    String defaultTag = PantryPins.getInstance().getConfig().getString("settings.default-tag");

                    if (menuUtil.getIdentifier() != null || !(menuUtil.getIdentifier().equalsIgnoreCase("none"))) {
                        Tag t = PantryPins.getInstance().getTagManager().getTag(menuUtil.getIdentifier());
                        if (t != null) {
                            t.removeEffects(menuUtil.getOwner());
                        }
                    }

                    UserData.setActive(player, defaultTag);
                    super.refresh();
                    menuUtil.setIdentifier(defaultTag);

                    if (PantryPins.getInstance().getConfig().getBoolean("settings.gui-messages")) {
                        msgPlayer(player, messages.getString("messages.reset-message").replaceAll("%prefix%", Objects.requireNonNull(messages.getString("messages.prefix"))));
                    }
                }
            }

            if (name.equalsIgnoreCase("sort")) {
                String current_sort = menuUtil.getSort();
                Set<String> rarities = PantryPins.getInstance().getRarityManager().getRarityMap().keySet();

                List<String> sortOptions = new ArrayList<>();
                sortOptions.add("none"); // no sorting
                for (String rarity : rarities) {
                    sortOptions.add("rarity:" + rarity);
                }

                int currentIndex = sortOptions.indexOf(current_sort == null ? "none" : current_sort.toLowerCase());
                if (currentIndex == -1) currentIndex = 0;

                int nextIndex = (currentIndex + 1) % sortOptions.size();
                String nextSort = sortOptions.get(nextIndex);

                menuUtil.setSort(nextSort);
                super.refresh();
            }

            if (name.equalsIgnoreCase("back")) {

                // If we're NOT on the first page, go back a page
                if (page != 0) {
                    page = page - 1;
                    super.refresh();
                    return;
                }

                // Otherwise (page == 1), go back to TagMenu / MainMenu
                player.closeInventory();
                boolean isCategories = PantryPins.getInstance().getConfig().getBoolean("settings.categories");

                if (isCategories) {
                    new MainMenu(PantryPins.getMenuUtil(player)).open();
                } else {
                    new TagMenu(PantryPins.getMenuUtil(player)).open();
                }
            }

            if (name.equalsIgnoreCase("next")) {
                if (variants.size() > maxItems && currentItemsOnPage >= maxItems) {
                    page = page + 1;
                    super.refresh();
                } else {
                    e.setCancelled(true);
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        getVariantsCountOnPage(tag.getIdentifier());
        applyLayout(false, false, true);

        if (this.variants == null || this.variants.isEmpty())
            return;

        String sort = menuUtil.getSort();

        // Filter variants based on sorting
        List<Variant> filtered = new ArrayList<>(variants);

        if (sort != null && sort.startsWith("rarity:")) {
            String rarity = sort.replace("rarity:", "");
            filtered.removeIf(v -> !v.getRarity().equalsIgnoreCase(rarity));
        }

        // Paging Setup
        int startIndex = page * maxItems;
        int endIndex = Math.min(startIndex + maxItems, filtered.size());

        currentItemsOnPage = 0;
        index = startIndex; // ✅ VERY IMPORTANT

        for (int i = startIndex; i < endIndex; i++) {
            if (i >= filtered.size()) break;

            Variant var = filtered.get(i);
            if (var == null) continue;

            // ------------------
            // BUILD ITEM (your original item-building logic left untouched)
            // ------------------

            String material;
            String item_displayname;
            int item_custom_model_data;

            boolean unlocked = menuUtil.getOwner().hasPermission(var.getPermission()) ||
                    var.getPermission().equalsIgnoreCase("none");

            // Model data
            item_custom_model_data = unlocked ? var.getUnlocked_custom_model_data() : var.getLocked_custom_model_data();

            // Display name
            item_displayname = unlocked ?
                    var.getUnlocked_displayname().replace("%tag%", var.getTag().get(0)) :
                    var.getLocked_displayname().replace("%tag%", var.getTag().get(0));

            // Material
            material = unlocked ?
                    (var.getUnlocked_material() != null ? var.getUnlocked_material() : "NAME_TAG") :
                    (var.getLocked_material() != null ? var.getLocked_material() : "BARRIER");

            ItemResolver.ResolvedItem resolved = ItemResolver.resolveCustomItem(menuUtil.getOwner(), material);
            ItemStack item = resolved.item();
            ItemMeta meta = resolved.meta();
            NBTItem nbt = new NBTItem(item);

            // Set model data
            if (meta != null) {
                if (unlocked) {
                    if (item_custom_model_data > 0) {
                        meta.setCustomModelData(item_custom_model_data);
                    }
                } else {
                    int modelData = guis.getInt("gui.tag-menu.global-locked-tag.custom-model-data");
                    if (modelData > 0) {
                        meta.setCustomModelData(modelData);
                    }
                }
            }

            // Lore building
            List<String> lore = getFormattedLore(var, var.getPermission());

            String joinedDescription = var.getDescription().stream()
                    .map(Utils::format)
                    .collect(Collectors.joining("\n"));

            for (int l = 0; l < lore.size(); l++) {
                String line = lore.get(l);

                if (line.equals("%description%")) {
                    List<String> desc = Arrays.asList(joinedDescription.split("\n"));
                    lore.remove(l);
                    lore.addAll(l, desc);
                    l += desc.size() - 1;
                    continue;
                } else {
                    line = line.replace("%description%", joinedDescription.replace("\n", " "));
                }

                line = line.replace("%identifier%", var.getIdentifier());
                line = line.replace("%tag%", var.getCurrentTag() != null ? var.getCurrentTag() : var.getTag().getFirst());
                line = line.replace("%variants%", String.valueOf(tag.getVariants().size()));
                line = line.replace("%order%", String.valueOf(tag.getOrder()));
                line = line.replace("%category%", tag.getCategory());
                line = line.replace("%rarity%", PantryPins.getInstance().getRarityManager().getRarity(var.getRarity()).getDisplayname());
                lore.set(l, line);
            }

            meta.setLore(color(lore));

            // Active glow
            if (UserData.getActive(menuUtil.getOwner().getUniqueId()).equalsIgnoreCase(var.getIdentifier()) &&
                    PantryPins.getInstance().getConfig().getBoolean("settings.active-tag-glow")) {
                meta.addEnchant(Enchantment.KNOCKBACK, 1, true);
            }

            meta.setDisplayName(format(item_displayname));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);

            nbt.getItem().setItemMeta(meta);

            nbt.setString("variant_identifier", var.getIdentifier());
            nbt.setBoolean("isVariant", true);

            this.inventory.addItem(nbt.getItem());

            currentItemsOnPage++;
        }
    }

    private List<String> getFormattedLore(Variant var, String permission) {
        List<String> lore;
        boolean hasPermission = menuUtil.getOwner().hasPermission(permission) || permission.equalsIgnoreCase("none");
        boolean isSelected = UserData.getActive(menuUtil.getOwner().getUniqueId()).equalsIgnoreCase(var.getIdentifier());

        String lorePath;

        lorePath = hasPermission ? (isSelected ? "selected-lore" : "unlocked-lore") : "locked-permission";

        lore = guis.getStringList("gui.tag-variants-menu.item.lore." + lorePath);

        return lore;
    }

    public List<Variant> getVariants() {
        return this.variants;
    }
}