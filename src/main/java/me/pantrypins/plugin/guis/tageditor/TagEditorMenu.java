package me.pantrypins.plugin.guis.tageditor;

import de.tr7zw.nbtapi.NBTItem;
import me.pantrypins.plugin.PantryPins;
import me.pantrypins.plugin.guis.MainMenu;
import me.pantrypins.plugin.guis.TagMenu;
import me.pantrypins.plugin.handlers.Tag;
import me.pantrypins.plugin.handlers.menu.MenuUtil;
import me.pantrypins.plugin.handlers.menu.Paged;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static me.pantrypins.plugin.utils.Utils.*;

public class TagEditorMenu extends Paged {

    private final Map<String, Tag> tags;

    private FileConfiguration guis = PantryPins.getInstance().getConfigManager().getConfig("guis.yml").get();

    public TagEditorMenu(MenuUtil menuUtil) {
        super(menuUtil);
        tags = PantryPins.getInstance().getTagManager().getTags();
    }

    @Override
    public String getMenuName() {
        String title = format(Objects.requireNonNull(guis.getString("gui.tag-editor-menu.title")).replaceAll("%page%", String.valueOf(this.getPage())));
        title = globalPlaceholders(menuUtil.getOwner(), title);
        return title;
    }

    @Override
    public int getSlots() {
        return 54;
    }

    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player)e.getWhoClicked();
        ArrayList<String> tag = new ArrayList<>(this.tags.keySet());
        String back = this.guis.getString("gui.items.back.displayname");
        String close = this.guis.getString("gui.items.close.displayname");
        String next = this.guis.getString("gui.items.next.displayname");
        String reset = this.guis.getString("gui.items.reset.displayname");
        String active = this.guis.getString("gui.items.active.displayname");
        NBTItem nbt = new NBTItem(e.getCurrentItem());

        if (nbt.hasTag("identifier")) {
            String identifier = nbt.getString("identifier");
            this.menuUtil.setIdentifier(identifier);
            (new SpecificTagMenu(PantryPins.getMenuUtilIdentifier(player, identifier))).open();
        }
        if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(format(close)))
            player.closeInventory();
        if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(format(back))) {
            if (this.page != 0) {
                this.page--;
                open();
            } else {
                player.closeInventory();
                boolean useCategories = PantryPins.getInstance().getConfig().getBoolean("settings.categories");
                if (useCategories) {
                    (new MainMenu(PantryPins.getMenuUtil(player))).open();
                } else {
                    (new TagMenu(PantryPins.getMenuUtil(player))).open();
                }
            }
        } else if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(format(next))) {
            if ((((tag.size() > this.maxItems) ? 1 : 0) & ((currentItemsOnPage >= this.maxItems) ? 1 : 0)) != 0) {
                if (this.index + 1 < tag.size()) {
                    this.page++;
                    open();
                } else {
                    e.setCancelled(true);
                }
            } else {
                e.setCancelled(true);
            }
        }
    }

    @Override
    public void setMenuItems() {
        getTagItemsEditor();
        applyEditorLayout();
    }
}