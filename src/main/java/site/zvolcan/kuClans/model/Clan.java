package site.zvolcan.kuClans.model;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.zvolcan.kuClans.KuClans;
import site.zvolcan.kuClans.manager.LanguageManager;
import site.zvolcan.kuClans.util.MessageUtil;

import java.util.*;
import java.util.regex.Pattern;

public final class Clan {

    private final int id;
    private final Map<UUID, RankType> players = new HashMap<>();
    private String displayName;
    private Location spawnLocation;
    private int kills = 0;
    private int deaths = 0;
    private final Inventory inv;

    public Clan(@NotNull String displayName, @Nullable Map<UUID, RankType> players, int id) {
        inv = Bukkit.createInventory(null, 36, Component.text("Clan Chest - " + displayName));
        this.displayName = displayName;
        if (players != null) {
            this.players.putAll(players);
        }
        this.id = id;
    }

    public Map<UUID, RankType> getPlayers() {
        return players;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public void joinMember(@NotNull Player player) {
        final KuClans plugin = KuClans.getPlugin();
        final LanguageManager languageManager = plugin.getLanguageManager();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (Player member : players.keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList()) {
                if (member != null) {
                    member.sendMessage(languageManager.getMessageWithoutPrefix(player, "clan.join-member-message")
                            .replaceText(TextReplacementConfig.builder()
                                    .replacement(player.getName()).match(Pattern.compile("\\{[A-Za-z]+}")).build()));
                }
            }
        });
    }

    public void leftMember(@NotNull Player player) {
        final KuClans plugin = KuClans.getPlugin();
        final LanguageManager languageManager = plugin.getLanguageManager();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (Player member : players.keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList()) {
                if (member != null) {
                    member.sendMessage(languageManager.getMessageWithoutPrefix(member, "clan.left-member-message")
                            .replaceText(TextReplacementConfig.builder()
                                    .replacement(player.getName()).match(Pattern.compile("\\{[A-Za-z]+}")).build()));
                }
            }
        });
    }

    @NotNull
    public UUID getLeader() {
        return players.entrySet().stream()
                .filter(entry -> entry.getValue() == RankType.LEADER)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(new ArrayList<>(players.keySet()).getFirst());
    }

    public RankType getRank(@NotNull OfflinePlayer player) {
        return players.getOrDefault(player.getUniqueId(), RankType.MEMBER);
    }

    public void invitePlayer(@NotNull Player lead, @NotNull Player player) {
        LanguageManager languageManager = KuClans.getPlugin().getLanguageManager();

        if (players.containsKey(player.getUniqueId())) {
            lead.sendMessage(languageManager.getMessage(player, "clan.already-in-clan"));
            return;
        }

        final PlayerData playerData = KuClans.getPlugin().getDataManager().getPlayerData(player);
        playerData.setClanIDInvite(String.valueOf(id));
        List<Component> inviteMessageList = Objects.requireNonNull(languageManager.getListString("invitation-to-the-join-clan")).stream().map((m) -> {
            m = PlaceholderAPI.setBracketPlaceholders(player, m);
            return MiniMessage.miniMessage().deserialize(m.replace("{clan}", getDisplayName()));
        }).toList();

        for (Component component : inviteMessageList) {
            player.sendMessage(component);
        }

        for (Player b : getOnlineMembers()) {
            b.sendMessage(languageManager.getMessage(player, "clan.invited-player"));
        }
    }

    public void kickMember(@NotNull Player lead, @NotNull OfflinePlayer player) {
        final LanguageManager languageManager = KuClans.getPlugin().getLanguageManager();

        if (players.containsKey(player.getUniqueId())) {
            lead.sendMessage(languageManager.getMessage(player, "clan.not-in-clan"));
        }

        players.remove(player.getUniqueId());
        if (player.isOnline()) {
            KuClans.getPlugin().getDataManager().generateData(player).setClan(null);
        }

        for (Player b : getOnlineMembers()) {
            b.sendMessage(languageManager.getMessage(player, "clan.member-left-clan"));
        }
    }

    public List<Player> getOnlineMembers() {
        return players.keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
    }

    public int getMemberCount() {
        return players.size();
    }

    public int getPoints() {
        return Math.max(0, kills - deaths);
    }

    public void addMember(@NotNull Player player) {
        KuClans plugin = KuClans.getPlugin();

        if (players.containsKey(player.getUniqueId())) {
            player.sendMessage(plugin.getLanguageManager().getMessage(player, "clan.already-in-clan"));
            return;
        }

        players.put(player.getUniqueId(), RankType.MEMBER);
        plugin.getDataManager().generateData(player).setClan(this);
        plugin.getClansManager().saveClan(this);
    }

    public Inventory getInv() {
        return inv;
    }

    public void sendMessage(String s) {
        for (Player player : getOnlineMembers()) {
            player.sendMessage(MessageUtil.translateColors(
                    KuClans.getPlugin().getConfig().getString("clan-chat.format", "<gray>[Clan] <white>{player}: <gray>{message}")
                            .replace("{clan}", getDisplayName())
                            .replace("{message}", s))
            );
        }
    }

    public void handlerDeath() {
        this.deaths += 1;
    }

    public void handlerKill() {
        this.kills += 1;
    }

    public int getID() {
        return id;
    }
}
