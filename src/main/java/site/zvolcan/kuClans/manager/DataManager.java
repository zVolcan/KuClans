package site.zvolcan.kuClans.manager;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import site.zvolcan.kuClans.model.PlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DataManager {

    private final Map<UUID, PlayerData> players = new HashMap<>();
    private final ClansManager clansManager;

    public DataManager(ClansManager clansManager) {
        this.clansManager = clansManager;
    }

    @NotNull
    public PlayerData generateData(@NotNull OfflinePlayer player) {
        PlayerData playerData = players.computeIfAbsent(player.getUniqueId(), k -> new PlayerData(player.getUniqueId()));
        playerData.setClan(clansManager.getClanByMember(player.getUniqueId()));
        return playerData;
    }

    @NotNull
    public PlayerData getPlayerData(@NotNull OfflinePlayer player) {
        PlayerData playerData = players.get(player.getUniqueId());

        if (playerData == null) {
            playerData = generateData(player);
        }

        return playerData;
    }

    public void removePlayerData(@NotNull OfflinePlayer player) {
        players.remove(player.getUniqueId());
    }

    public void shutDown() {
        players.clear();
    }
}
