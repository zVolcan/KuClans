package site.zvolcan.kuClans.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.NotNull;
import site.zvolcan.kuClans.KuClans;
import site.zvolcan.kuClans.PlaceholdersKuClans;
import site.zvolcan.kuClans.commands.impl.CommandImpl;
import site.zvolcan.kuClans.manager.LanguageManager;

public final class MainCommand implements CommandImpl {

    private final LanguageManager languageManager;
    private final PlaceholdersKuClans placeholdersKuClans;
    private final KuClans plugin;

    public MainCommand(LanguageManager languageManager, PlaceholdersKuClans placeholdersKuClans, KuClans plugin) {
        this.languageManager = languageManager;
        this.placeholdersKuClans = placeholdersKuClans;
        this.plugin = plugin;
    }

    @Override
    public @NotNull LiteralCommandNode<CommandSourceStack> command() {
        LiteralArgumentBuilder<CommandSourceStack> literal = Commands.literal("kuclans")
                .requires((ctx) -> ctx.getSender().hasPermission("kuclans.admin"));
        literal.then(reload());
        return literal.build();
    }

    @NotNull
    private LiteralCommandNode<CommandSourceStack> reload() {
        LiteralArgumentBuilder<CommandSourceStack> literal = Commands.literal("reload");

        literal.then(Commands.literal("messages").executes(ctx -> {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                languageManager.loadMessages(plugin.getConfig().getString("lang", "en_us"));
                ctx.getSource().getSender().sendMessage(languageManager.getMessage(null, "success-reload-messages"));
            });

            return Command.SINGLE_SUCCESS;
        }));

        literal.then(Commands.literal("all").executes(ctx -> {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                plugin.reloadConfig();
                languageManager.loadMessages(plugin.getConfig().getString("lang", "en_us"));
                ctx.getSource().getSender().sendMessage(languageManager.getMessage(null, "success-reload-all"));
            });

            return Command.SINGLE_SUCCESS;
        }));

        literal.then(Commands.literal("placeholders").executes(ctx -> {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                placeholdersKuClans.unregister();
                placeholdersKuClans.register();
                ctx.getSource().getSender().sendMessage(languageManager.getMessage(null, "success-reload-placeholders"));
            });

            return Command.SINGLE_SUCCESS;
        }));

        return literal.build();
    }
}
