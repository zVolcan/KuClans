package site.zvolcan.kuClans.manager;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.zvolcan.kuClans.KuClans;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import me.neznamy.yamlassist.YamlAssist;
import site.zvolcan.kuClans.util.MessageUtil;

public final class LanguageManager {

    private final KuClans plugin;
    private final Map<String, String> messages = new HashMap<>();
    private boolean presentPlaceholderAPI = false;

    public LanguageManager(@NotNull KuClans plugin) {
        this.plugin = plugin;

        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            presentPlaceholderAPI = true;
        }

        plugin.saveResource("lang/en_us.yml", false);
        plugin.saveResource("lang/es_es.yml", false);
        loadMessages(plugin.getConfig().getString("lang", "en_us"));
    }

    public void loadMessages(String key) {
        messages.clear();
        File file = new File(plugin.getDataFolder() + "/lang/", key + ".yml");

        if (!file.exists()) {
            plugin.saveResource("lang/" + key + ".yml", false);
            file = new File(plugin.getDataFolder() + "/lang/", key + ".yml");
        }

        YamlConfiguration yml = new YamlConfiguration();

        try {
            yml.load(file);

            // Usar getKeys(true) para recuperar también claves anidadas (path con puntos)
            for (String path : yml.getKeys(true)) {
                // Si el valor es un string simple
                if (yml.isString(path)) {
                    messages.put(path, yml.getString(path));
                } else if (yml.isList(path)) {
                    // Si el valor es una lista, unir en un único string separado por saltos de línea
                    List<String> list = yml.getStringList(path);
                    messages.put(path, String.join("\n", list));
                }
                // Ignorar otras secciones/complejos; getKeys(true) ya devuelve rutas completas
            }
        } catch (FileNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "File not found", e);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "IO Exception", e);
        } catch (InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Invalid configuration", e);
            List<String> suggestions = YamlAssist.getSuggestions(file);

            for (String s : suggestions) {
                plugin.getLogger().info(s);
            }
        }
    }

    private final MiniMessage mm = MiniMessage.miniMessage();
    private final LegacyComponentSerializer lcs = LegacyComponentSerializer.legacyAmpersand();

    public Component getMessage(@Nullable OfflinePlayer player, String key) {
        if (messages.isEmpty()) {
            throw new RuntimeException("Messages are not loaded");
        }

        String message = messages.get(key);

        if (message == null) {
            return Component.text(key);
        }

        if (player != null && presentPlaceholderAPI) {
            message = PlaceholderAPI.setPlaceholders(player, message);

            if (player.isOnline()) {
                message = message.replace("{player}", Objects.requireNonNull(player.getName()));
            }
        }

        String prefix = messages.get("prefix");
        if (prefix != null) {
            message = prefix + message;
        }

        /*if (player instanceof Player b && plugin.getConfig().getBoolean("sounds-messages-enabled")) {
            if (message.startsWith("<red>") || message.startsWith("&c")) {
                b.playSound(b.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, .7F, 1.5F);
            } else if (message.startsWith("<green>") || message.startsWith("<green>")) {
                b.playSound(b.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, .7F, 2F);
            }
        } */

        return MessageUtil.translateColors(message);
    }

    @Nullable
    public List<String> getListString(String key) {
        File file = new File(plugin.getDataFolder() + "/lang/", plugin.getConfig().getString("lang", "en_us") + ".yml");

        if (!file.exists()) {
            plugin.saveResource("lang/" + plugin.getConfig().getString("lang", "en_us") + ".yml", false);
            file = new File(plugin.getDataFolder() + "/lang/", plugin.getConfig().getString("lang", "en_us") + ".yml");
        }

        YamlConfiguration yml = new YamlConfiguration();

        try {
            yml.load(file);

            if (yml.isList(key)) {
                return yml.getStringList(key);
            }
        } catch (FileNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "File not found", e);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "IO Exception", e);
        } catch (InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, "Invalid configuration", e);
            List<String> suggestions = YamlAssist.getSuggestions(file);

            for (String s : suggestions) {
                plugin.getLogger().info(s);
            }
        }

        return null;
    }

    public Component getMessageWithoutPrefix(@Nullable Player player, String key) {
        if (messages.isEmpty()) {
            throw new RuntimeException("Messages are not loaded");
        }

        String message = messages.get(key);

        if (message == null) {
            return Component.text(key);
        }

        if (presentPlaceholderAPI && player != null) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }

        if (player != null && plugin.getConfig().getBoolean("sounds-messages-enabled")) {
            if (message.startsWith("<red>") || message.startsWith("&c")) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, .7F, 1.5F);
            } else if (message.startsWith("<green>") || message.startsWith("<green>")) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, .7F, 2F);
            }
        }

        if (message.contains("<") && message.contains(">")) {
            return mm.deserialize(message);
        } else {
            return lcs.deserialize(message);
        }
    }
}
