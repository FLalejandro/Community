package me.novoro.community.commands.fun;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.novoro.community.api.Location;
import me.novoro.community.commands.CommandBase;
import me.novoro.community.config.LangManager;
import me.novoro.community.utils.LocationUtil;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.Map;

/**
 * Summons lightning either where the player is looking or on the targetted players.
 * "The single most important command." - Sun Tzu, The Art of War
 */
public class SmiteCommand extends CommandBase {
    public SmiteCommand() {
        super("smite", "community.smite", 2);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return command.executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            Location lookingAt = LocationUtil.getLookingAt(player);
            this.summonLightning(new Location(player.getServerWorld(), lookingAt.getX(), lookingAt.getY(), lookingAt.getZ()));
            LangManager.sendLang(context.getSource(), "Smite-Message");
            return Command.SINGLE_SUCCESS;
        }).then(argument("target", EntityArgumentType.players())
                .requires(source -> this.permission(source, "community.smitetargets", 4))
                .executes(context -> {
                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "target");
                    players.forEach(player -> {
                        this.summonLightning(new Location(player));
                        LangManager.sendLang(player, "Smited-Message");
                    });
                    if (players.size() == 1) {
                        ServerPlayerEntity firstPlayer = players.iterator().next();
                        LangManager.sendLang(context.getSource(), "Smite-Player-Message", Map.of("{player}", firstPlayer.getName().getString()));
                    } else LangManager.sendLang(context.getSource(), "Smite-Multiple-Message", Map.of("{amount}", String.valueOf(players.size())));

                    return Command.SINGLE_SUCCESS;
                })
        );
    }

    private void summonLightning(Location location) {
        LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, location.getWorld());
        lightning.setPos(location.getX(), location.getY(), location.getZ());
        location.getWorld().spawnEntity(lightning);
    }
}