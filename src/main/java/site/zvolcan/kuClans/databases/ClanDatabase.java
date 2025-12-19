package site.zvolcan.kuClans.databases;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import site.zvolcan.kuClans.KuClans;
import site.zvolcan.kuClans.model.Clan;
import site.zvolcan.kuClans.model.RankType;

import javax.annotation.Nullable;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ClanDatabase {

    private final KuClans plugin;
    private final Gson gson = new Gson();

    public ClanDatabase(KuClans plugin) {
        this.plugin = plugin;
        try {
            createTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable() throws SQLException {
        try (Connection connection = plugin.getHikari().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS clans (" +
                             "id INT AUTO_INCREMENT PRIMARY KEY," +
                             "name VARCHAR(16) NOT NULL UNIQUE," +
                             "spawn_location VARCHAR(255)," +
                             "kills INT DEFAULT 0," +
                             "deaths INT DEFAULT 0" +
                             ");"
             );
             PreparedStatement statement2 = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS clan_members (" +
                             "clan_id INT," +
                             "player_uuid VARCHAR(36) NOT NULL," +
                             "rank VARCHAR(16) NOT NULL," +
                             "FOREIGN KEY (clan_id) REFERENCES clans(id) ON DELETE CASCADE" +
                             ");"
             )) {
            statement.execute();
            statement2.execute();
        }
    }

    @Nullable
    public Clan getClanByMember(UUID playerUUID) {
        try (Connection connection = plugin.getHikari().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT c.* FROM clans c " +
                             "JOIN clan_members cm ON c.id = cm.clan_id " +
                             "WHERE cm.player_uuid = ?")) {
            statement.setString(1, playerUUID.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return clanFromResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveClan(Clan clan) {
        try (Connection connection = plugin.getHikari().getConnection()) {
            int clanId = getClanId(connection, clan.getDisplayName());

            if (clanId == -1) {
                // Insert new clan
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO clans (name, spawn_location, kills, deaths) VALUES (?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, clan.getDisplayName());
                    statement.setString(2, locationToString(clan.getSpawnLocation()));
                    statement.setInt(3, clan.getKills());
                    statement.setInt(4, clan.getDeaths());
                    statement.executeUpdate();

                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            clanId = generatedKeys.getInt(1);
                        }
                    }
                }
            } else {
                // Update existing clan
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE clans SET spawn_location = ?, kills = ?, deaths = ? WHERE id = ?")) {
                    statement.setString(1, locationToString(clan.getSpawnLocation()));
                    statement.setInt(2, clan.getKills());
                    statement.setInt(3, clan.getDeaths());
                    statement.setInt(4, clanId);
                    statement.executeUpdate();
                }
            }

            if (clanId != -1) {
                // Update members
                try (PreparedStatement deleteStatement = connection.prepareStatement(
                        "DELETE FROM clan_members WHERE clan_id = ?")) {
                    deleteStatement.setInt(1, clanId);
                    deleteStatement.executeUpdate();
                }

                try (PreparedStatement insertStatement = connection.prepareStatement(
                        "INSERT INTO clan_members (clan_id, player_uuid, rank) VALUES (?, ?, ?)")) {
                    for (Map.Entry<UUID, RankType> entry : clan.getPlayers().entrySet()) {
                        insertStatement.setInt(1, clanId);
                        insertStatement.setString(2, entry.getKey().toString());
                        insertStatement.setString(3, entry.getValue().name());
                        insertStatement.addBatch();
                    }
                    insertStatement.executeBatch();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteClan(Clan clan) {
        try (Connection connection = plugin.getHikari().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM clans WHERE name = ?")) {
            statement.setString(1, clan.getDisplayName());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Clan> loadClans() {
        List<Clan> clans = new ArrayList<>();
        try (Connection connection = plugin.getHikari().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM clans");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                clans.add(clanFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clans;
    }

    private Clan clanFromResultSet(ResultSet resultSet) throws SQLException {
        String name = resultSet.getString("name");
        Location spawn = stringToLocation(resultSet.getString("spawn_location"));
        int clanId = resultSet.getInt("id");

        Map<UUID, RankType> members = new HashMap<>();
        try (Connection connection = plugin.getHikari().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT player_uuid, rank FROM clan_members WHERE clan_id = ?")) {
            statement.setInt(1, clanId);
            try (ResultSet membersResultSet = statement.executeQuery()) {
                while (membersResultSet.next()) {
                    members.put(UUID.fromString(membersResultSet.getString("player_uuid")),
                            RankType.valueOf(membersResultSet.getString("rank")));
                }
            }
        }

        Clan clan = new Clan(name, members, clanId);
        clan.setSpawnLocation(spawn);
        clan.setKills(resultSet.getInt("kills"));
        clan.setDeaths(resultSet.getInt("deaths"));


        return clan;
    }

    private int getClanId(Connection connection, String name) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM clans WHERE name = ?")) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        }
        return -1;
    }

    private String locationToString(Location location) {
        if (location == null) {
            return null;
        }
        return location.getWorld().getName() + "," +
                location.getX() + "," +
                location.getY() + "," +
                location.getZ() + "," +
                location.getYaw() + "," +
                location.getPitch();
    }

    private Location stringToLocation(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        String[] parts = string.split(",");
        World world = Bukkit.getWorld(parts[0]);
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        float yaw = Float.parseFloat(parts[4]);
        float pitch = Float.parseFloat(parts[5]);
        return new Location(world, x, y, z, yaw, pitch);
    }
}
