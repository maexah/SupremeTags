package me.pantrypins.plugin.guis.search;

import de.tr7zw.nbtapi.NBTItem;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import me.pantrypins.plugin.PantryPins;
import me.pantrypins.plugin.api.events.TagAssignEvent;
import me.pantrypins.plugin.api.events.TagBuyEvent;
import me.pantrypins.plugin.api.events.TagResetEvent;
import me.pantrypins.plugin.guis.personaltags.PersonalTagsMenu;
import me.pantrypins.plugin.guis.tagactions.TagActionsMenu;
import me.pantrypins.plugin.guis.variant.TagVariantsMenu;
import me.pantrypins.plugin.handlers.Tag;
import me.pantrypins.plugin.handlers.menu.MenuUtil;
import me.pantrypins.plugin.handlers.menu.Paged;
import me.pantrypins.plugin.managers.TagManager;
import me.pantrypins.plugin.storage.UserData;
import me.pantrypins.plugin.utils.CompatUtils;
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
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static me.pantrypins.plugin.utils.Utils.*;
import static me.pantrypins.plugin.utils.Utils.color;

public class SearchResultMenu extends Paged {

    private final Map<String, Tag> tags;

    private FileConfiguration messages = PantryPins.getInstance().getConfigManager().getConfig("messages.yml").get();
    private FileConfiguration guis = PantryPins.getInstance().getConfigManager().getConfig("guis.yml").get();

    public SearchResultMenu(MenuUtil menuUtil, String searchResult) {
        super(menuUtil);
        menuUtil.setSearchResult(searchResult);
        tags = PantryPins.getInstance().getTagManager().getTags();

        enableAutoUpdate(true);
    }

    @Override
    public String getMenuName() {
        return format("Search Result: " + menuUtil.getSearchResult());
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();

        if (e.getClickedInventory().getType() == InventoryType.PLAYER) {
            e.setCancelled(true); // Cancel the event to prevent any actions within the player's inventory
            return;
        }

        String back = guis.getString("gui.items.back.displayname");
        String close = guis.getString("gui.items.close.displayname");
        String next = guis.getString("gui.items.next.displayname");
        String reset = guis.getString("gui.items.reset.displayname");
        String ptags = guis.getString("gui.items.personal-tags.displayname");
        String search = guis.getString("gui.items.search.displayname");

        String insufficient = messages.getString("messages.insufficient-funds").replaceAll("%prefix%", Objects.requireNonNull(messages.getString("messages.prefix")));
        String unlocked = messages.getString("messages.tag-unlocked").replaceAll("%prefix%", Objects.requireNonNull(messages.getString("messages.prefix")));

        insufficient = replacePlaceholders(player, insufficient);

        if (e.getCurrentItem().getType().equals(Material.valueOf(Objects.requireNonNull(guis.getString("gui.items.glass.material")).toUpperCase()))) {
            e.setCancelled(true);
        }

        ArrayList<String> tag = new ArrayList<>(tags.keySet());

        String no_tag_selected = messages.getString("messages.no-tag-selected").replaceAll("%prefix%", Objects.requireNonNull(messages.getString("messages.prefix")));

        NBTItem nbt = new NBTItem(e.getCurrentItem());
        if (nbt.hasTag("identifier")) {
            String identifier = nbt.getString("identifier");

            Tag t = PantryPins.getInstance().getTagManager().getTag(identifier);

            boolean tagActionsEnabled = guis.getBoolean("gui.tag-actions-menu.enable");
            boolean isCostTag = t.getEconomy().isEnabled();

            if (tagActionsEnabled) {
                if (!menuUtil.getOwner().hasPermission(t.getPermission()) && !t.isCostTag()) {
                    if (PantryPins.getInstance().getConfig().getBoolean("settings.gui-messages")) {
                        String locked = messages.getString("messages.locked-tag").replaceAll("%prefix%", Objects.requireNonNull(messages.getString("messages.prefix")));
                        locked = replacePlaceholders(menuUtil.getOwner(), locked);
                        msgPlayer(player, locked);
                    }
                    return;
                }

                player.closeInventory();
                new TagActionsMenu(PantryPins.getMenuUtilIdentifier(player, identifier)).open();
                return;
            }

            if (e.getClick().isRightClick() && t.getVariants().size() > 0) {
                player.closeInventory();
                new TagVariantsMenu(PantryPins.getMenuUtil(player), t).open();
                return;
            } else {

                if (!isCostTag) {
                    if (player.hasPermission(t.getPermission()) || t.getPermission().equalsIgnoreCase("none")) {
                        if (!UserData.getActive(player.getUniqueId()).equalsIgnoreCase(identifier) && identifier != null) {

                            TagAssignEvent tagevent = new TagAssignEvent(player, identifier, false);
                            Bukkit.getPluginManager().callEvent(tagevent);

                            if (tagevent.isCancelled()) return;

                            UserData.setActive(player, tagevent.getTag());

                            super.refresh();
                            menuUtil.setIdentifier(tagevent.getTag());

                            if (PantryPins.getInstance().getConfig().getBoolean("settings.gui-messages")) {
                                String select = messages.getString("messages.tag-select-message").replaceAll("%prefix%", Objects.requireNonNull(messages.getString("messages.prefix")));
                                select = replacePlaceholders(menuUtil.getOwner(), select);
                                msgPlayer(player, select.replace("%identifier%", identifier).replaceAll("%tag%", PantryPins.getInstance().getTagManager().getTag(identifier).getCurrentTag()));
                            }

                            playConfigSound(player, "selected-tag");
                        } else if (UserData.getActive(player.getUniqueId()).equalsIgnoreCase(identifier) && PantryPins.getInstance().isDeactivateClick()) {

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

                            playConfigSound(player, "reset-tag");
                        }
                    } else {
                        if (PantryPins.getInstance().getConfig().getBoolean("settings.gui-messages")) {
                            String locked = messages.getString("messages.locked-tag").replaceAll("%prefix%", Objects.requireNonNull(messages.getString("messages.prefix")));
                            locked = replacePlaceholders(menuUtil.getOwner(), locked);
                            msgPlayer(player, locked);
                        }

                        playConfigSound(player, "error-message");
                    }
                } else {
                    if (player.hasPermission(t.getPermission()) || t.getPermission().equalsIgnoreCase("none")) {
                        if (!UserData.getActive(player.getUniqueId()).equalsIgnoreCase(identifier) && identifier != null) {
                            TagAssignEvent tagevent = new TagAssignEvent(player, identifier, false);
                            Bukkit.getPluginManager().callEvent(tagevent);

                            if (tagevent.isCancelled()) return;

                            UserData.setActive(player, tagevent.getTag());
                            super.refresh();
                            menuUtil.setIdentifier(tagevent.getTag());

                            if (PantryPins.getInstance().getConfig().getBoolean("settings.gui-messages")) {
                                String select = messages.getString("messages.tag-select-message").replaceAll("%prefix%", Objects.requireNonNull(messages.getString("messages.prefix")));
                                select = replacePlaceholders(menuUtil.getOwner(), select);
                                msgPlayer(player, select.replace("%identifier%", identifier).replaceAll("%tag%", PantryPins.getInstance().getTagManager().getTag(identifier).getCurrentTag()));
                            }
                        } else if (UserData.getActive(player.getUniqueId()).equalsIgnoreCase(identifier) && PantryPins.getInstance().isDeactivateClick()) {
                            if (player.hasPermission(t.getPermission()) || t.getPermission().equalsIgnoreCase("none")) {

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
                            } else {
                                if (PantryPins.getInstance().getConfig().getBoolean("settings.gui-messages")) {
                                    String locked = messages.getString("messages.locked-tag").replaceAll("%prefix%", Objects.requireNonNull(messages.getString("messages.prefix")));
                                    locked = replacePlaceholders(menuUtil.getOwner(), locked);
                                    msgPlayer(player, locked);
                                }
                            }
                        }
                    } else {
                        double cost = t.getEconomy().getAmount();

                        // check if they have the right amount of money to buy etc....
                        if (hasAmount(player, t.getEconomy().getType(), t.getEconomy().getAmount(), t.getIdentifier())) {
                            // give them the tag

                            TagBuyEvent tagevent = new TagBuyEvent(player, identifier, cost, false);
                            Bukkit.getPluginManager().callEvent(tagevent);

                            if (tagevent.isCancelled()) return;

                            take(player, t.getEconomy().getType(), t.getEconomy().getAmount(), t.getIdentifier());
                            addPerm(player, t.getPermission());

                            if (PantryPins.getInstance().getConfig().getBoolean("settings.gui-messages")) {
                                msgPlayer(player, unlocked.replaceAll("%identifier%", t.getIdentifier()).replaceAll("%tag%", PantryPins.getInstance().getTagManager().getTag(identifier).getCurrentTag()));
                            }

                            super.refresh();
                        } else {
                            if (PantryPins.getInstance().getConfig().getBoolean("settings.gui-messages")) {
                                insufficient = replacePlaceholders(menuUtil.getOwner(), insufficient);
                                msgPlayer(player, insufficient.replaceAll("%cost%", String.valueOf(t.getEconomy().getAmount())));
                            }
                        }
                    }
                }
            }
        }

        if (isCustomGUIItemSlot(menuUtil.getOwner(), e.getCurrentItem()) == e.getSlot()) {
            String name = isCustomGUIItemName(menuUtil.getOwner(), e.getCurrentItem());

            List<String> click_commands = guis.getStringList("gui.tag-menu.custom-items." + name + ".click-commands");

            for (String option : click_commands) {
                if (option.startsWith("[message]")) {
                    String message = option.replace("[message] ", "");
                    message = replacePlaceholders(menuUtil.getOwner(), message);
                    msgPlayer(menuUtil.getOwner(), message);
                }

                if (option.startsWith("[player]")) {
                    String command = option.replace("[player] ", "");
                    command = command.replaceAll("%player%", menuUtil.getOwner().getName());
                    menuUtil.getOwner().performCommand(command);
                }

                if (option.startsWith("[console]")) {
                    String command = option.replace("[console] ", "");
                    command = command.replaceAll("%player%", menuUtil.getOwner().getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("%player%", menuUtil.getOwner().getName()));
                }

                if (option.startsWith("[broadcast]")) {
                    String message = option.replace("[message] ", "");
                    message = replacePlaceholders(menuUtil.getOwner(), message);
                    Bukkit.broadcastMessage(message);
                }

                if (option.startsWith("[close]")) {
                    menuUtil.getOwner().closeInventory();
                }
            }
            return;
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
                        t.removeEffects(menuUtil.getOwner());
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

            if (name.equalsIgnoreCase("back")) {
                if (page != 0) {
                    page = page - 1;
                    super.refresh();
                }
            }

            if (name.equalsIgnoreCase("next")) {
                if (tag.size() > maxItems & currentItemsOnPage >= maxItems) {
                    if (!((index + 1) >= tag.size())) {
                        page = page + 1;
                        super.refresh();
                    } else {
                        e.setCancelled(true);
                    }
                } else {
                    e.setCancelled(true);
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        getTagsCountOnPage();
        applyLayout(false, false, false);
        getTagItems();
    }

    public void getTagItems() {
        Map<String, Tag> tags = PantryPins.getInstance().getTagManager().getTags();

        List<Tag> tagList;

        String searchTerm = menuUtil.getSearchResult();
        if (searchTerm != null && !searchTerm.isEmpty()) {
            tagList = tags.values().stream()
                    .filter(t -> t.getIdentifier().toLowerCase().contains(searchTerm.toLowerCase()) ||
                            t.getCategory().toLowerCase().contains(searchTerm.toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            tagList = new ArrayList<>(tags.values());
        }

        if (PantryPins.getInstance().getConfig().getBoolean("settings.only-show-player-access-tags")) {
            tagList = tagList.stream()
                    .filter(t -> menuUtil.getOwner().hasPermission(t.getPermission()))
                    .collect(Collectors.toList());
        }

        if (!tagList.isEmpty()) {
            int maxItemsPerPage = guis.getInt("gui.tag-menu.tags-per-page");
            int startIndex = page * maxItemsPerPage;
            int endIndex = Math.min(startIndex + maxItemsPerPage, tagList.size());

            tagList.sort(getTagComparator(PantryPins.getInstance().getConfig().getBoolean("settings.prioritise-selected-tag"), menuUtil));

            currentItemsOnPage = 0;
            List<String> slots = guis.getStringList("gui.tag-menu.slots-tag.slots");

            for (int i = startIndex; i < endIndex; i++) {
                if (i >= tagList.size()) {
                    break;
                }

                Tag t = tagList.get(i);
                if (t == null) break;

                String permission = PantryPins.getInstance().getTagManager().getTagConfig().getString("tags." + t.getIdentifier() + ".permission");
                if (permission != null && !PantryPins.getInstance().getConfig().getBoolean("settings.locked-view") && !PantryPins.getInstance().getConfig().getBoolean("settings.cost-system") && !menuUtil.getOwner().hasPermission(permission))
                    continue;

                // Process display name, material, lore, and other settings (as shown in original code)
                String displayname;
                if (menuUtil.getOwner().hasPermission(t.getPermission()) || permission.equalsIgnoreCase("none")) {
                    if (PantryPins.getInstance().getTagManager().getTagConfig().getString("tags." + t.getIdentifier() + ".displayname") != null) {
                        if (t.getCurrentTag() != null) {
                            displayname = Objects.requireNonNull(PantryPins.getInstance().getTagManager().getTagConfig().getString("tags." + t.getIdentifier() + ".displayname")).replace("%tag%", t.getCurrentTag());
                        } else {
                            displayname = Objects.requireNonNull(PantryPins.getInstance().getTagManager().getTagConfig().getString("tags." + t.getIdentifier() + ".displayname")).replace("%tag%", t.getTag().get(0));
                        }
                    } else {
                        displayname = format("&7Tag: " + t.getCurrentTag());
                    }
                } else {
                    if (PantryPins.getInstance().getTagManager().getTagConfig().getString("gui.tag-menu.global-locked-tag.displayname") == null) {
                        if (t.getCurrentTag() != null) {
                            displayname = Objects.requireNonNull(PantryPins.getInstance().getTagManager().getTagConfig().getString("tags." + t.getIdentifier() + ".displayname")).replace("%tag%", t.getCurrentTag());
                        } else {
                            displayname = Objects.requireNonNull(PantryPins.getInstance().getTagManager().getTagConfig().getString("tags." + t.getIdentifier() + ".displayname")).replace("%tag%", t.getTag().get(0));
                        }
                    } else {
                        if (t.getCurrentTag() != null) {
                            displayname = Objects.requireNonNull(PantryPins.getInstance().getTagManager().getTagConfig().getString("gui.tag-menu.global-locked-tag.displayname")).replace("%tag%", t.getCurrentTag());
                        } else {
                            displayname = Objects.requireNonNull(PantryPins.getInstance().getTagManager().getTagConfig().getString("gui.tag-menu.global-locked-tag.displayname")).replace("%tag%", t.getTag().get(0));
                        }
                    }
                }

                if (PantryPins.getInstance().isPlaceholderAPI()) {
                    displayname = replacePlaceholders(menuUtil.getOwner(), displayname);
                }

                if (PantryPins.getInstance().isItemsAdder()) {
                    displayname = FontImageWrapper.replaceFontImages(displayname);
                }

                String material;

                if (menuUtil.getOwner().hasPermission(t.getPermission()) || permission.equalsIgnoreCase("none") ) {
                    if (PantryPins.getInstance().getTagManager().getTagConfig().getString("tags." + t.getIdentifier() + ".display-item") != null) {
                        material = PantryPins.getInstance().getTagManager().getTagConfig().getString("tags." + t.getIdentifier() + ".display-item");
                    } else {
                        material = "NAME_TAG";
                    }
                } else {
                    if (PantryPins.getInstance().getTagManager().getTagConfig().getString("gui.tag-menu.global-locked-tag.display-item") != null) {
                        material = PantryPins.getInstance().getTagManager().getTagConfig().getString("gui.tag-menu.global-locked-tag.display-item");
                    } else {
                        material = "NAME_TAG";
                    }
                }

                assert permission != null;

                ItemResolver.ResolvedItem resolved = ItemResolver.resolveCustomItem(menuUtil.getOwner(), material);
                ItemStack tagItem = resolved.item();
                ItemMeta tagMeta = resolved.meta();
                NBTItem nbt = new NBTItem(tagItem);

                nbt.setString("identifier", t.getIdentifier());

                if (menuUtil.getOwner().hasPermission(t.getPermission()) || permission.equalsIgnoreCase("none") ) {
                    if (PantryPins.getInstance().getTagManager().getTagConfig().getInt("tags." + t.getIdentifier() + ".custom-model-data") > 0) {
                        int modelData = PantryPins.getInstance().getTagManager().getTagConfig().getInt("tags." + t.getIdentifier() + ".custom-model-data");
                        if (tagMeta != null)
                            tagMeta.setCustomModelData(modelData);
                    }
                } else {
                    if (PantryPins.getInstance().getTagManager().getTagConfig().getInt("gui.tag-menu.global-locked-tag.custom-model-data") > 0) {
                        int modelData = PantryPins.getInstance().getTagManager().getTagConfig().getInt("gui.tag-menu.global-locked-tag.custom-model-data");
                        if (tagMeta != null)
                            tagMeta.setCustomModelData(modelData);
                    }
                }

                assert tagMeta != null;

                if (UserData.getActive(menuUtil.getOwner().getUniqueId()).equalsIgnoreCase(t.getIdentifier()) && PantryPins.getInstance().getConfig().getBoolean("settings.active-tag-glow")) {
                    tagMeta.addEnchant(Enchantment.KNOCKBACK, 1, true);
                }

                tagMeta.setDisplayName(format(displayname));
                tagMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                try {
                    ItemFlag hideDye = ItemFlag.valueOf("HIDE_DYE");
                    tagMeta.addItemFlags(hideDye);
                } catch (IllegalArgumentException ignored) {
                    // HIDE_DYE not available in this version — skip
                }
                tagMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                tagMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

                List<String> lore = getFormattedLore(t, permission);

                String descriptionPlaceholder = "%description%";
                String identifierPlaceholder = "%identifier%";
                String tagPlaceholder = "%tag%";
                String costPlaceholder = "%cost%";
                String costFormattedPlaceholder = "%cost_formatted%";
                String costFormattedPlaceholderRaw = "%cost_formatted_raw%";
                String variantsPlaceholder = "%variants%";
                String orderPlaceholder = "%order%";
                String trackPlaceholder = "%track_unlocked%";
                String effectsPlaceholder = "%effects%";
                String categoryPlaceholder = "%category%";
                String rarityPlaceholder = "%rarity%";
                String effectsListPlaceholder = "%effects_list%";
                String joinedDescription = t.getDescription().stream().map(Utils::format).collect(Collectors.joining("\n"));

                String joinedEffects;

                String effects_list = "";

                if (!t.getEffects().isEmpty()) {
                    String formatEffectTemplate = messages.getString("messages.effects-replace-style");

                    joinedEffects = t.getEffects().keySet().stream()
                            .map(PotionEffectType::getName)
                            .map(Utils::format)
                            .map(effect -> formatEffectTemplate.replace("%effect%", effect))
                            .collect(Collectors.joining("\n"));

                    effects_list = t.getEffects().keySet().stream()
                            .map(CompatUtils::getEffectKey)
                            .collect(Collectors.joining(", "));
                } else {
                    joinedEffects = format(messages.getString("messages.no-effects"));
                    effects_list = format(messages.getString("messages.no-effects"));
                }

                for (int l = 0; l < lore.size(); l++) {
                    String line = lore.get(l);

                    // Pattern for %custom-placeholder_?%
                    Pattern customPlaceholderPattern = Pattern.compile("%custom-placeholder_(.*?)%");
                    Matcher matcher = customPlaceholderPattern.matcher(line);

                    while (matcher.find()) {
                        String dynamicPart = matcher.group(1);
                        line = line.replace(matcher.group(0), t.getCustomPlaceholder(t.getIdentifier(), dynamicPart));
                    }

                    if (line.contains(descriptionPlaceholder)) {
                        List<String> descriptionLines = Arrays.asList(joinedDescription.split("\n"));

                        lore.remove(l);

                        lore.addAll(l, descriptionLines);

                        l += descriptionLines.size() - 1;
                    } else if (line.contains(effectsPlaceholder)) {
                        List<String> effectLines = Arrays.asList(joinedEffects.split("\n"));

                        lore.remove(l);

                        lore.addAll(l, effectLines);

                        l += effectLines.size() - 1;
                    } else {
                        line = line.replace(identifierPlaceholder, t.getIdentifier());
                        if (t.getCurrentTag() != null) {
                            line = line.replace(tagPlaceholder, t.getCurrentTag());
                        } else {
                            line = line.replace(tagPlaceholder, t.getTag().getFirst());
                        }
                        line = line.replace(costFormattedPlaceholder, "$" + formatNumber(t.getEconomy().getAmount()));
                        line = line.replace(costFormattedPlaceholderRaw, formatNumber(t.getEconomy().getAmount()));
                        line = line.replace(costPlaceholder, String.valueOf(t.getEconomy().getAmount()));
                        line = line.replace(variantsPlaceholder, String.valueOf(t.getVariants().size()));
                        line = line.replace(orderPlaceholder, String.valueOf(t.getOrder()));
                        line = line.replace(trackPlaceholder, String.valueOf(TagManager.tagUnlockCounts.getOrDefault(t.getIdentifier(), 0)));
                        line = line.replace(categoryPlaceholder, t.getCategory());
                        line = line.replace(effectsListPlaceholder, effects_list);
                        line = line.replace(rarityPlaceholder, PantryPins.getInstance().getRarityManager().getRarity(t.getRarity()).getDisplayname());
                        line = globalPlaceholders(menuUtil.getOwner(), line);

                        lore.set(l, line);
                    }
                }

                tagMeta.setLore(color(lore));
                nbt.getItem().setItemMeta(tagMeta);
                nbt.setString("identifier", t.getIdentifier());

                int placementSlot;

                boolean useDefinedSlots = guis.getBoolean("gui.tag-menu.slots-tag.enable");

                if (useDefinedSlots && currentItemsOnPage < slots.size()) {
                    String rawSlot = slots.get(currentItemsOnPage);
                    try {
                        placementSlot = Integer.parseInt(rawSlot);
                    } catch (NumberFormatException e) {
                        placementSlot = inventory.firstEmpty();
                    }
                } else if (useDefinedSlots) {
                    placementSlot = inventory.firstEmpty();
                } else {
                    placementSlot = inventory.firstEmpty();
                }

                if (placementSlot != -1) {
                    inventory.setItem(placementSlot, nbt.getItem());
                    if (t.isAnimated()) {
                        animatedSlots.add(placementSlot);
                    }
                }

                currentItemsOnPage++;
            }
        }
    }
}