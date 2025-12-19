package site.zvolcan.kuClans.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import site.zvolcan.kuClans.manager.DataManager;
import site.zvolcan.kuClans.model.Clan;

public final class PlayerConnectListener implements Listener {

    private final DataManager dataManager;

    public PlayerConnectListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        Clan clan = dataManager.generateData(player).getClan();
        if (clan != null) {
            clan.joinMember(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        dataManager.removePlayerData(player);
        Clan clan = dataManager.generateData(player).getClan();
        if (clan != null) {
            clan.leftMember(player);
        }
    }
}
