package site.zvolcan.kuClans;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.zvolcan.kuClans.manager.DataManager;
import site.zvolcan.kuClans.model.PlayerData;
import site.zvolcan.kuClans.util.ReplaceString;

public final class PlaceholdersKuClans extends PlaceholderExpansion {

    private final KuClans plugin;
    private final DataManager dataManager;

    public PlaceholdersKuClans(KuClans plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "kuclans";
    }

    @Override
    public @NotNull String getAuthor() {
        return "volcqnn";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Nullable
    public String onPlaceholderRequest(final Player player, @NotNull final String params) {
        if (player == null) return null;

        final PlayerData playerData = dataManager.getPlayerData(player);

        return switch (params.toLowerCase()) {
            case "in_clan" -> playerData.isClanChat() ? "yes" : "no";
            case "name" ->  playerData.getClan().getDisplayName();
            case "display_name" -> ReplaceString.getDisplayName(playerData.getClan().getDisplayName());

            default -> null;
        };
    }
}
