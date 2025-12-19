package site.zvolcan.kuClans.inventories.invite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
import org.jetbrains.annotations.NotNull;
import site.zvolcan.kuClans.KuClans;
import site.zvolcan.kuClans.manager.LanguageManager;
import site.zvolcan.kuClans.model.PlayerData;

public final class AcceptInviteInventory implements InventoryHolder, Listener {

    private final Inventory inv;

    public AcceptInviteInventory(final @NotNull KuClans plugin) {
        this.inv = Bukkit.createInventory(this, 36, Component.text("Accept Invite"));
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        fillInventory();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }

    private void fillInventory() {
        final ItemStack accept = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        accept.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<green>Accept").decoration(TextDecoration.ITALIC, false));
        });

        for (int i = 0; i < inv.getSize(); i++) {
            if (i == 4 || i == 13 || i == 22 || i == 31) {
                i+=5;
            }

            if (i >= inv.getSize()) {
                break;
            }

            inv.setItem(i,accept);
        }

        final ItemStack deny = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        deny.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize("<red>Deny").decoration(TextDecoration.ITALIC, false));
        });

        for (int i = 5; i < inv.getSize(); i++) {
            if (i == 4 || i == 13 || i == 22 || i == 31) {
                i+=4;
            }

            inv.setItem(i,deny);
        }

        final ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        pane.editMeta(meta -> {
            meta.displayName(Component.text(" "));
        });
    }

    public void openInventory(final @NotNull Player player) {
        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() != this) return;

        ItemStack item = event.getCurrentItem();
        Player b = (Player) event.getWhoClicked();
        PlayerData playerData = KuClans.getPlugin().getDataManager().getPlayerData(b);
        final LanguageManager languageManager = KuClans.getPlugin().getLanguageManager();

        if (item != null && event.getInventory().getHolder() == this) {
            event.setCancelled(true);

            switch (item.getType()) {
                case RED_STAINED_GLASS_PANE -> {
                    event.setCancelled(true);
                    if (playerData.getClanIDInvite() == null) {
                        b.sendMessage(languageManager.getMessage(b, "no-pending-invitations"));
                        b.closeInventory();
                        return;
                    }

                    playerData.setClanIDInvite(null);
                    b.sendMessage(languageManager.getMessage(b, "invitation-declined"));
                    b.closeInventory();
                }

                case GREEN_STAINED_GLASS_PANE -> {
                    event.setCancelled(true);
                    if (playerData.getClanIDInvite() == null) {
                        b.sendMessage(languageManager.getMessage(b, "no-pending-invitations"));
                        b.closeInventory();
                        return;
                    }

                    int id = Integer.parseInt(playerData.getClanIDInvite());
                    KuClans.getPlugin().getClansManager().getClans().stream().filter(c -> c.getID() == id).findFirst().ifPresent(clan -> {
                        clan.addMember(b);
                        for (Player member : clan.getOnlineMembers()) {
                            member.sendMessage(languageManager.getMessage(b, "clan.member-joined-clan"));
                        }
                        playerData.setClanIDInvite(null);
                        b.sendMessage(languageManager.getMessage(b, "invitation-accepted"));
                        b.closeInventory();
                    });

                    b.closeInventory();
                }

                default -> {
                    event.setCancelled(true);
                }
            }
        }
    }
}
