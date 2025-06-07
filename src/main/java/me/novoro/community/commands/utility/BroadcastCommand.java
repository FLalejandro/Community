package me.novoro.community.commands.utility;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.community.commands.CommandBase;
import me.novoro.community.config.LangManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.novoro.community.utils.ColorUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;

import static me.novoro.community.Seam.getServer;

/**
 * Provides a command to broadcast messages to all players on the server.
 */
public class BroadcastCommand extends CommandBase {
    public BroadcastCommand() {
        super("broadcast", "community.broadcast", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.then(
                argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            //ToDo: Neo set up adventure for dis pls
                            String message = StringArgumentType.getString(ctx, "message");
                            message = LangManager.getLangSafely("Broadcast-Prefix") + message;
                            Component adventureComponent = ColorUtil.parseColour(message);

                            Collection<ServerPlayerEntity> players = getServer().getPlayerManager().getPlayerList();
                            for (ServerPlayerEntity player : players) {
                                Audience audience = player;
                                audience.sendMessage(adventureComponent);
                            }

                            return 1;
                        })
        );
    }
}
