package me.novoro.community.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.community.Seam;
import me.novoro.community.utils.ColorUtil;
import net.minecraft.server.command.ServerCommandSource;

/**
 * Seam's reload command.
 */
public final class CommunityReloadCommand extends CommandBase {
    public CommunityReloadCommand() {
        super("community", "community.reload", 4);
    }

    @Override
    public boolean bypassCommandCheck() {
        return true;
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.then(literal("reload")
                .executes(context -> {
                    Seam.inst().reloadConfigs();
                    context.getSource().sendMessage(ColorUtil.parseColour(Seam.MOD_PREFIX + "&aReloaded Configs!"));
                    return Command.SINGLE_SUCCESS;
                }));
    }
}
