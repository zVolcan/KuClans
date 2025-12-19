package site.zvolcan.kuClans.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import site.zvolcan.kuClans.commands.impl.CommandImpl;
import site.zvolcan.kuClans.manager.DataManager;
import site.zvolcan.kuClans.manager.LanguageManager;
import site.zvolcan.kuClans.model.Clan;
import site.zvolcan.kuClans.model.PlayerData;

public final class ClanChatCommand implements CommandImpl {

    private final DataManager dataManager;
    private final LanguageManager languageManager;

    public ClanChatCommand(DataManager dataManager, LanguageManager languageManager) {
        this.dataManager = dataManager;
        this.languageManager = languageManager;
    }

    @Override
    public @NotNull LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("clanchat")
                .executes((ctx) -> {
                    if (!(ctx.getSource().getSender() instanceof Player player)) return Command.SINGLE_SUCCESS;

                    final PlayerData playerData = dataManager.generateData(player);
                    Clan clan = playerData.getClan();
                    if (clan == null) {
                        player.sendMessage(languageManager.getMessage(player, "not-in-clan"));
                        return Command.SINGLE_SUCCESS;
                    }

                    if (playerData.isClanChat()) {
                        player.sendMessage(languageManager.getMessage(player, "clan-chat-enabled"));
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, .5F, 1.6F);
                        playerData.setClanChat(false);
                    } else {
                        player.sendMessage(languageManager.getMessage(player, "clan-chat-disabled"));
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, .5F, 1.6F);
                        playerData.setClanChat(true);
                    }

                    return Command.SINGLE_SUCCESS;
                }).build();
    }
}
