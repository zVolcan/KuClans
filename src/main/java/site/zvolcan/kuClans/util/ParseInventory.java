package site.zvolcan.kuClans.util;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class ParseInventory {

    public static Inventory createInventory(final @NotNull YamlConfiguration yml, @Nullable InventoryHolder holder) {
        if (!checkOptions(yml)) {
            throw new RuntimeException("Invalid inventory options");
        }

        final Inventory inv = Bukkit.createInventory(holder, yml.getInt("size"), MiniMessage.miniMessage().deserialize(Objects.requireNonNull(yml.getString("title"))));

        return inv;
    }

    private static boolean checkOptions(final @NotNull YamlConfiguration yml) {
        return yml.contains("size") && yml.contains("title") && yml.contains("items");
    }

    private static ItemStack getItem(final @NotNull YamlConfiguration yml, @NotNull String key) {
        if (!yml.contains(key)) {
            return null;
        }



        return null;
    }

}
