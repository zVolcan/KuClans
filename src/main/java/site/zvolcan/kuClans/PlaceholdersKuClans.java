package site.zvolcan.kuClans;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.zvolcan.kuClans.manager.ClansManager;
import site.zvolcan.kuClans.manager.DataManager;
import site.zvolcan.kuClans.model.Clan;
import site.zvolcan.kuClans.model.PlayerData;
import site.zvolcan.kuClans.util.ReplaceString;

import java.util.List;

public final class PlaceholdersKuClans extends PlaceholderExpansion {

    private final KuClans plugin;
    private final DataManager dataManager;
    private final ClansManager clansManager;

    public PlaceholdersKuClans(KuClans plugin, DataManager dataManager, ClansManager clansManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.clansManager = clansManager;
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

        if (params.startsWith("top_")) {
            final List<Clan> clans = clansManager.getClansOrderByPoints();

            String top = params.replace("top_", "");
            int position;
            try {
                position = Integer.parseInt(top) - 1;
            } catch (NumberFormatException e) {
                return null;
            }

            if (position < 0 || position >= clans.size()) {
                return null;
            }

            Clan clan = clans.get(position);
            return switch (params.substring(params.indexOf("_", 4) + 1).toLowerCase()) {
                case "name" -> clan.getDisplayName();
                case "display_name" -> ReplaceString.getDisplayName(clan.getDisplayName());
                case "points" -> String.valueOf(clan.getPoints());
                case "kills" -> String.valueOf(clan.getKills());
                case "deaths" -> String.valueOf(clan.getDeaths());
                case "leader" -> Bukkit.getOfflinePlayer(clan.getLeader()).getName();
                default -> null;
            };
        }

        if (params.equalsIgnoreCase("in_clan") || params.equalsIgnoreCase("display_name")) {
            {
                return switch (params.toLowerCase()) {
                    case "in_clan" -> playerData.isClanChat() ? "yes" : "no";
                    case "display_name" -> ReplaceString.getDisplayName(playerData.getClan().getDisplayName());
                    default -> null;
                };
            }
        }

        if (playerData.getClan() == null) {
            return switch (params.toLowerCase()) {
                case "in_clan" -> "no";
                case "name", "display_name" -> "No Clan";
                default -> null;
            };
        }

        if (params.startsWith("clan_")) {
            final Clan clan = playerData.getClan();
            return switch (params.toLowerCase().replace("clan_", "")) {
                case "name" -> clan.getDisplayName();
                case "display_name" -> ReplaceString.getDisplayName(clan.getDisplayName());
                case "points" -> String.valueOf(clan.getPoints());
                case "kills" -> String.valueOf(clan.getKills());
                case "deaths" -> String.valueOf(clan.getDeaths());
                case "leader" -> Bukkit.getOfflinePlayer(clan.getLeader()).getName();
                default -> null;
            };
        }

        return null;
    }
}
