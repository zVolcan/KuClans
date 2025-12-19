package site.zvolcan.kuClans.inventories;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.references.Items;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import site.zvolcan.kuClans.KuClans;
import site.zvolcan.kuClans.manager.ClansManager;
import site.zvolcan.kuClans.model.Clan;
import site.zvolcan.kuClans.util.MessageUtil;

import java.util.List;

public final class ClansInventory implements Listener {

    private final ClansManager clansManager;

    public ClansInventory(ClansManager clansManager) {
        this.clansManager = clansManager;
        Bukkit.getPluginManager().registerEvents(this, KuClans.getPlugin());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        title = title.replace("[", "").replace("]", "");

        if (title.startsWith("Clans - Page ")) {
            event.setCancelled(true);
            final Player player = (Player) event.getWhoClicked();

            final int currentPage = Integer.parseInt(title.replace("Clans - Page ", ""));
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                return;
            }

            if (event.getCurrentItem().getType() == Material.ARROW) {
                if (PlainTextComponentSerializer.plainText().serialize(event.getCurrentItem().displayName()).contains("Next Page")) {
                    openInventoryPage(currentPage + 1, player);
                } else if (PlainTextComponentSerializer.plainText().serialize(event.getCurrentItem().displayName()).contains("Previous Page")) {
                    openInventoryPage(currentPage - 1, player);
                }
            }
        }

    }

    public void openInventoryPage(final int page, final Player player) {
        final Inventory inv = Bukkit.createInventory(null, 54, Component.text("Clans - Page " + page));

        int top = 1 + (45 * (page - 1));
        for (Clan clan : clansManager.getClansOrderByPoints().stream().skip(Long.max(0, (page - 1) * 45L)).limit(45).toList()) {
            final ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            int finalTop = top;
            item.editMeta(SkullMeta.class, meta -> {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(clan.getLeader()));
                meta.displayName(MessageUtil.translateColors("<green>#"+ finalTop +" - " + clan.getDisplayName()).decoration(TextDecoration.ITALIC, false));
                meta.lore(
                        List.of(
                                MessageUtil.translateColors("&7Points: &6" + clan.getPoints()).decoration(TextDecoration.ITALIC, false),
                                MessageUtil.translateColors("&7Miembros: &a" + clan.getMemberCount()).decoration(TextDecoration.ITALIC, false),
                                MessageUtil.translateColors("&7Leader: &c" + Bukkit.getOfflinePlayer(clan.getLeader()).getName()).decoration(TextDecoration.ITALIC, false)
                        )
                );
            });

            inv.addItem(item);
            top++;
        }

        final ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        pane.editMeta(meta -> {
            meta.displayName(Component.text(" "));
        });

        for (int i = 45; i < inv.getSize(); i++) {
           inv.setItem(i, pane);
        }

        final ItemStack nextPage = new ItemStack(Material.ARROW);
        nextPage.editMeta(meta -> {
            meta.displayName(MessageUtil.translateColors("<green>Next Page").decoration(TextDecoration.ITALIC, false));
        });

        final ItemStack previousPage = new ItemStack(Material.ARROW);
        previousPage.editMeta(meta -> {
            meta.displayName(MessageUtil.translateColors("<green>Previous Page").decoration(TextDecoration.ITALIC, false));
        });

        if (clansManager.getClansOrderByPoints().size() > page * 45) {
            inv.setItem(53, nextPage);
        }

        if (page > 1) {
            inv.setItem(45, previousPage);
        }

        Bukkit.getScheduler().runTask(KuClans.getPlugin(), () -> {
            player.openInventory(inv);
            player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 2);
        });
    }

}
