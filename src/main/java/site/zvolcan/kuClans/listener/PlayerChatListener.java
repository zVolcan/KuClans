package site.zvolcan.kuClans.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import site.zvolcan.kuClans.KuClans;
import site.zvolcan.kuClans.manager.DataManager;
import site.zvolcan.kuClans.model.PlayerData;

public final class PlayerChatListener implements Listener {

    private final DataManager dataManager;
    private final KuClans plugin;

    public PlayerChatListener(DataManager dataManager, KuClans plugin) {
        this.dataManager = dataManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        final PlayerData playerData = dataManager.getPlayerData(event.getPlayer());

        if (playerData.isClanChat() && playerData.getClan() != null && plugin.getConfig().getBoolean("clan-chat.enabled")) {
            event.setCancelled(true);
            playerData.getClan().sendMessage(PlainTextComponentSerializer.plainText().serialize(event.message()).replace("[", "").replace("]", ""));
        }
    }

}
