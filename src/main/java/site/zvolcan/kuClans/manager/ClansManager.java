package site.zvolcan.kuClans.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import site.zvolcan.kuClans.KuClans;
import site.zvolcan.kuClans.databases.ClanDatabase;
import site.zvolcan.kuClans.inventories.ClansInventory;
import site.zvolcan.kuClans.model.Clan;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ClansManager {

    private final List<Clan> clans = new ArrayList<>();
    private final ClanDatabase clanDatabase;

    // INVENTORIES
    private final ClansInventory clansInventory = new ClansInventory(this);

    public ClansManager(KuClans plugin) {
        this.clanDatabase = new ClanDatabase(plugin);
        clans.addAll(clanDatabase.loadClans());
    }

    public Clan getClanByMember(UUID memberUUID) {
        return clans.stream()
                .filter(clan -> clan.getPlayers().containsKey(memberUUID))
                .findFirst()
                .orElse(null);
    }

    public void saveClan(Clan clan) {
        clanDatabase.saveClan(clan);
        if (!clans.contains(clan)) {
            clans.add(clan);
        }
    }

    @Nullable
    public Clan getClanByName(String name) {
        return clans.stream()
                .filter(clan -> clan.getDisplayName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public void deleteClan(Clan clan) {
        clanDatabase.deleteClan(clan);
        clans.remove(clan);
    }

    public List<Clan> getClans() {
        return clans;
    }

    public List<Clan> getClansOrderByPoints() {
        List<Clan> sortedClans = new ArrayList<>(clans);
        sortedClans.sort((c1, c2) -> Integer.compare(c2.getPoints(), c1.getPoints()));
        return new ArrayList<>(sortedClans);
    }

    public void shutDown() {
        for (Clan clan : clans) {
            saveClan(clan);
        }

        clans.clear();
    }

    public void openClansInventory(final Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(KuClans.getPlugin(), () -> {
            clansInventory.openInventoryPage(1, player);
        });
    }
}
