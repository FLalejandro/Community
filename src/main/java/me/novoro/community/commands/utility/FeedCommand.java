package me.novoro.community.commands.utility;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.community.commands.CommandBase;
import me.novoro.community.config.LangManager;
import me.novoro.community.config.SettingsManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.Map;

/**
 * Provides command to feed the player.
 */
public class FeedCommand extends CommandBase {
    public FeedCommand() {
        super("feed", "community.feed", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            FeedCommand.feedPlayer(context.getSource().getPlayerOrThrow());
            return Command.SINGLE_SUCCESS;
        }).then(argument("target", EntityArgumentType.players())
                .requires(source -> this.permission(source, "community.feedtargets", 4))
                .executes(context -> {
                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "target");
                    players.forEach(FeedCommand::feedPlayer);
                    if (players.size() == 1) {
                        ServerPlayerEntity firstPlayer = players.iterator().next();
                        LangManager.sendLang(context.getSource(), "Feed-Other-Message", Map.of("{player}", firstPlayer.getName().getString()));
                    } else LangManager.sendLang(context.getSource(), "Feed-All-Message", Map.of("{amount}", String.valueOf(players.size())));

                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    /**
     * Feeds the target players.
     *
     * @param target The target players.
     */
    private static void feedPlayer(ServerPlayerEntity target) {
        // Set Hunger to max
        target.getHungerManager().setFoodLevel(20);
        if (SettingsManager.feedFillsSaturation()) target.getHungerManager().setSaturationLevel(20);
        LangManager.sendLang(target, "Feed-Self-Message");
    }
}
