package site.zvolcan.kuClans.commands.impl;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public interface CommandImpl {

    @NotNull LiteralCommandNode<CommandSourceStack> command();

}
