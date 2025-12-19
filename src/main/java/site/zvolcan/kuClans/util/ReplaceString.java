package site.zvolcan.kuClans.util;

import org.bukkit.configuration.file.FileConfiguration;
import site.zvolcan.kuClans.KuClans;

public final class ReplaceString {

    public static String getDisplayName(String name) {
        final FileConfiguration config = KuClans.getPlugin().getConfig();
        String format = config.getString("format-display-clan-name"," <gray>[<white>{clan}<gray>]");
        return format.replace("{clan}", name);
    }

}
