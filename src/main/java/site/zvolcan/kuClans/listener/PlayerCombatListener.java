package site.zvolcan.kuClans.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;
import site.zvolcan.kuClans.manager.DataManager;
import site.zvolcan.kuClans.model.PlayerData;

public final class PlayerCombatListener implements Listener {

    private final DataManager dataManager;

    public PlayerCombatListener(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        final Player player = event.getPlayer();
        PlayerData playerData = dataManager.getPlayerData(player);

        if (playerData.getClan() != null) {
            playerData.getClan().handlerKill();
        }
    }

    private void killEvent(@NotNull final Player killer) {
        final PlayerData playerData = dataManager.getPlayerData(killer);

        if (playerData.getClan() != null) {
            playerData.getClan().handlerKill();
        }
    }

}
