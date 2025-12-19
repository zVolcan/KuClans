package site.zvolcan.kuClans.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import site.zvolcan.kuClans.KuClans;
import site.zvolcan.kuClans.commands.impl.CommandImpl;
import site.zvolcan.kuClans.inventories.invite.AcceptInviteInventory;
import site.zvolcan.kuClans.manager.ClansManager;
import site.zvolcan.kuClans.manager.LanguageManager;
import site.zvolcan.kuClans.model.Clan;
import site.zvolcan.kuClans.model.RankType;

import java.util.Map;
import java.util.regex.Pattern;

public final class ClanCommand implements CommandImpl {

    private final KuClans plugin;
    private final ClansManager clansManager;
    private final LanguageManager languageManager;
    private final AcceptInviteInventory acceptInviteInventory;

    public ClanCommand(KuClans plugin, ClansManager clansManager, LanguageManager languageManager) {
        this.plugin = plugin;
        this.clansManager = clansManager;
        this.acceptInviteInventory = new AcceptInviteInventory(plugin);
        this.languageManager = languageManager;
    }

    @Override
    public @NotNull LiteralCommandNode<CommandSourceStack> command() {
        final LiteralArgumentBuilder<CommandSourceStack> literal = Commands.literal("clan");

        literal.then(accept());
        literal.then(leave());
        literal.then(invite());
        literal.then(kick());
        literal.then(create());

        literal.executes((ctx) -> {
            if (!(ctx.getSource().getSender() instanceof Player player)) return Command.SINGLE_SUCCESS;
            clansManager.openClansInventory(player);
            return Command.SINGLE_SUCCESS;
        });

        return literal.build();
    }

    private LiteralCommandNode<CommandSourceStack> invite() {
        final LiteralArgumentBuilder<CommandSourceStack> literal = Commands.literal("invite");

        literal.then(Commands.argument("player", ArgumentTypes.player()).executes((ctx) -> {
            if (!(ctx.getSource().getSender() instanceof Player player)) {
                ctx.getSource().getSender().sendMessage(languageManager.getMessage(null, "not-console-command"));
                return Command.SINGLE_SUCCESS;
            }

            Clan clan = clansManager.getClanByMember(player.getUniqueId());

            if (clan == null) {
                player.sendMessage(languageManager.getMessage(null, "clan.not-in-clan"));
                return Command.SINGLE_SUCCESS;
            }

            if (clan.getRank(player) == RankType.MEMBER) {
                player.sendMessage(languageManager.getMessage(player, "clan.not-has-rank-required"));
                return Command.SINGLE_SUCCESS;
            }

            int maxInClan = plugin.getConfig().getInt("max-members-per-clan");
            if (maxInClan != -1 && maxInClan >= clan.getPlayers().size()) {
                player.sendMessage(languageManager.getMessage(player, "clan.not-has-rank-required"));
                return Command.SINGLE_SUCCESS;
            }

            PlayerSelectorArgumentResolver resolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
            final Player invited = resolver.resolve(ctx.getSource()).getFirst();

            if (plugin.getDataManager().getPlayerData(player).getClan() != null) {
                player.sendMessage(languageManager.getMessage(player, "clan.already-in-clan"));
            }

            if (invited == player) {
                player.sendMessage(languageManager.getMessage(player, "clan.not-to-invite-myself"));
                return Command.SINGLE_SUCCESS;
            }

            clan.invitePlayer(player, invited);
            return Command.SINGLE_SUCCESS;
        }));

        return literal.build();
    }

    private LiteralCommandNode<CommandSourceStack> kick() {
        final LiteralArgumentBuilder<CommandSourceStack> literal = Commands.literal("kick");

        literal.then(Commands.argument("player", ArgumentTypes.player()).executes((ctx) -> {
            if (!(ctx.getSource().getSender() instanceof Player player)) {
                ctx.getSource().getSender().sendMessage(languageManager.getMessage(null, "not-console-command"));
                return Command.SINGLE_SUCCESS;
            }

            Clan clan = clansManager.getClanByMember(player.getUniqueId());

            if (clan == null) {
                player.sendMessage(languageManager.getMessage(null, "clan.not-in-clan"));
                return Command.SINGLE_SUCCESS;
            }

            if (clan.getRank(player) == RankType.MEMBER) {
                player.sendMessage(languageManager.getMessage(player, "clan.not-has-rank-required"));
                return Command.SINGLE_SUCCESS;
            }

            int maxInClan = plugin.getConfig().getInt("max-members-per-clan");
            if (maxInClan != -1 && maxInClan >= clan.getPlayers().size()) {
                player.sendMessage(languageManager.getMessage(player, "clan.not-has-rank-required"));
                return Command.SINGLE_SUCCESS;
            }

            PlayerSelectorArgumentResolver resolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
            final Player kicked = resolver.resolve(ctx.getSource()).getFirst();

            if (clan.getRank(kicked) == clan.getRank(player) || clan.getRank(kicked) == RankType.LEADER) {
                player.sendMessage(languageManager.getMessage(player, "clan.not-has-rank-required"));
                return Command.SINGLE_SUCCESS;
            }

            if (kicked == player) {
                player.sendMessage(languageManager.getMessage(player, "not-to-invite-myself"));
                return Command.SINGLE_SUCCESS;
            }

            clan.kickMember(player, kicked);
            return Command.SINGLE_SUCCESS;
        }));

        return literal.build();
    }

    private LiteralCommandNode<CommandSourceStack> accept() {
        LiteralArgumentBuilder<CommandSourceStack> literal = Commands.literal("accept");

        literal.then(Commands.argument("clan", StringArgumentType.word()).executes((ctx) -> {
            if (!(ctx.getSource().getSender() instanceof Player player)) return Command.SINGLE_SUCCESS;

            String clanName = StringArgumentType.getString(ctx, "clan");
            final Clan clan = clansManager.getClanByName(clanName);
            if (clan == null) {
                player.sendMessage(languageManager.getMessage(player, "clan.does-not-exist"));
                return Command.SINGLE_SUCCESS;
            }

            acceptInviteInventory.openInventory(player);
            return Command.SINGLE_SUCCESS;
        }));

        return literal.build();
    }

    private LiteralCommandNode<CommandSourceStack> create() {
        final LiteralArgumentBuilder<CommandSourceStack> literal = Commands.literal("create");

        literal.then(Commands.argument("name", StringArgumentType.word()).executes((ctx) -> {
            if (!(ctx.getSource().getSender() instanceof Player player)) return Command.SINGLE_SUCCESS;

            String clanName = StringArgumentType.getString(ctx, "name");
            final Clan clan = new Clan(clanName, Map.of(player.getUniqueId(), RankType.LEADER), clansManager.getClans().size() + 1);
            plugin.getDataManager().getPlayerData(player).setClan(clan);
            clansManager.getClans().add(clan);
            player.sendMessage(languageManager.getMessage(player, "clan.created-successfully")
                    .replaceText(TextReplacementConfig.builder()
                            .replacement(clanName).match(Pattern.compile("\\{[A-Za-z]+}", Pattern.CASE_INSENSITIVE)).build()));

            return Command.SINGLE_SUCCESS;
        }));

        return literal.build();
    }

    private LiteralCommandNode<CommandSourceStack> leave() {
        LiteralArgumentBuilder<CommandSourceStack> literal = Commands.literal("leave");

        literal.executes((ctx) -> {
            if (!(ctx.getSource().getSender() instanceof Player player)) return Command.SINGLE_SUCCESS;

            Clan clan = clansManager.getClanByMember(player.getUniqueId());
            if (clan == null) {
                player.sendMessage(languageManager.getMessage(player, "clan.not-in-clan"));
                return Command.SINGLE_SUCCESS;
            }

            clan.kickMember(player, player);
            return Command.SINGLE_SUCCESS;
        });

        return literal.build();
    }
}