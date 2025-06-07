package me.novoro.community;

import com.mojang.brigadier.CommandDispatcher;
import me.novoro.community.api.configuration.Configuration;
import me.novoro.community.api.configuration.YamlConfiguration;
import me.novoro.community.api.permissions.DefaultPermissionProvider;
import me.novoro.community.api.permissions.LuckPermsPermissionProvider;
import me.novoro.community.api.permissions.PermissionProvider;
import me.novoro.community.commands.CommunityReloadCommand;
import me.novoro.community.commands.fun.SmiteCommand;
import me.novoro.community.commands.utility.*;
import me.novoro.community.config.LangManager;
import me.novoro.community.config.ModuleManager;
import me.novoro.community.config.SettingsManager;
import me.novoro.community.utils.CommunityLogger;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Community implements ModInitializer {
    public static final String MOD_PREFIX = "&8&l[<gradient:#96B8C3:#7C93A2>&lSᴇᴀᴍ</gradient>&8&l]&f ";

    private static Community instance;
    private MinecraftServer server;
    private PermissionProvider permissionProvider = null;

    private final LangManager langManager = new LangManager();
    private final ModuleManager moduleManager = new ModuleManager();
    private final SettingsManager settingsManager = new SettingsManager();

    @Override
    public void onInitialize() {
        Community.instance = this;

        // Proudly display Community Branding in everyone's console
        this.displayAsciiArt();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            this.server = server;
            this.checkPermissionProvider();
            this.reloadConfigs();
        });

        // Reloads modules on startup. Needs to be called before commands are registered.
        this.moduleManager.reload();

        // Registers all of Community's commands.
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> this.registerCommands(dispatcher));
    }

    /**
     * Displays an ASCII Art representation of the mod's name in the log.
     */
    private void displayAsciiArt() {
        CommunityLogger.info("   _____ ______          __  __  ");
        CommunityLogger.info("  / ____|  ____|   /\\   |  \\/  | ");
        CommunityLogger.info(" | (___ | |__     /  \\  | \\  / | ");
        CommunityLogger.info("  \\___ \\|  __|   / /\\ \\ | |\\/| | ");
        CommunityLogger.info("  ____) | |____ / ____ \\| |  | | ");
        CommunityLogger.info(" |_____/|______/_/    \\_\\_|  |_| ");
    }


    // Reloads Community's various configs.
    public void reloadConfigs() {
        // Lang
        this.langManager.reload();
        // Settings
        this.settingsManager.reload();
        // ToDo: Reload our *other* configs lol
    }

    // Registers Community's commands. Commands that are disabled are not registered.
    private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Reload Command
        new CommunityReloadCommand().register(dispatcher);

        // Fun Commands
        new SmiteCommand().register(dispatcher);

        // Utility Commands
        new BroadcastCommand().register(dispatcher);
        new CheckTimeCommand().register(dispatcher);
        new ClearInventoryCommand().register(dispatcher);
        new FeedCommand().register(dispatcher);
        new HealCommand().register(dispatcher);
    }

    /**
     * Gets Community's current instance. It is not recommended to use externally.
     */
    public static Community inst() {
        return Community.instance;
    }

    /**
     * Gets the current {@link MinecraftServer} Community is currently running on.
     */
    public static MinecraftServer getServer() {
        return Community.instance.server;
    }

    /**
     * Gets the {@link PermissionProvider} Community is currently using.
     */
    public static PermissionProvider getPermissionProvider() {
        return Community.instance.permissionProvider;
    }

    /**
     * Sets what {@link PermissionProvider} Community will use to handle all permissions.
     */
    public static void setPermissionProvider(PermissionProvider provider) {
        Community.instance.permissionProvider = provider;
        CommunityLogger.info("Registered " + provider.getName() + " as Community's permission provider.");
    }

    // Checks the server for the built-in permission providers.
    private void checkPermissionProvider() {
        if (this.permissionProvider != null) return;
        try {
            Class.forName("net.luckperms.api.LuckPerms");
            this.permissionProvider = new LuckPermsPermissionProvider();
            CommunityLogger.info("Found LuckPerms! Permission support enabled.");
            return;
        } catch (ClassNotFoundException ignored) {}
        this.permissionProvider = new DefaultPermissionProvider();
        CommunityLogger.warn("Couldn't find a built in permission provider.. falling back to permission levels.");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public File getDataFolder() {
        File folder = FabricLoader.getInstance().getConfigDir().resolve("Community").toFile();
        if (!folder.exists()) folder.mkdirs();
        return folder;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public File getFile(String fileName) {
        File file = new File(this.getDataFolder(), fileName);
        if (!file.exists()) file.getParentFile().mkdirs();
        return file;
    }

    public Configuration getConfig(String fileName, boolean saveResource) {
        File configFile = this.getFile(fileName);
        if (!configFile.exists()) {
            if (!saveResource) return null;
            this.saveResource(fileName, false);
        }
        return this.getConfig(configFile);
    }

    public Configuration getConfig(File configFile) {
        try {
            return YamlConfiguration.loadConfiguration(configFile); // ?
        } catch (IOException e) {
            CommunityLogger.error("Something went wrong getting the config: " + configFile.getName() + ".");
            CommunityLogger.printStackTrace(e);
        }
        return null;
    }

    public void saveConfig(String fileName, Configuration config) {
        File file = this.getFile(fileName);
        try {
            YamlConfiguration.save(config, file);
        } catch (IOException e) {
            CommunityLogger.warn("Something went wrong saving the config: " + fileName + ".");
            CommunityLogger.printStackTrace(e);
        }
    }

    @SuppressWarnings("resource")
    public void saveResource(String fileName, boolean overwrite) {
        File file = this.getFile(fileName);
        if (file.exists() && !overwrite) return;
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            Path path = Paths.get("configurations", fileName);
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(path.toString().replace("\\", "/"));
            assert in != null;
            in.transferTo(outputStream);
        } catch (IOException e) {
            CommunityLogger.error("Something went wrong saving the resource: " + fileName + ".");
            CommunityLogger.printStackTrace(e);
        }
    }
}
