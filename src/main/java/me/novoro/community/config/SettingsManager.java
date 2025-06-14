package me.novoro.community.config;

import me.novoro.community.api.configuration.Configuration;
import me.novoro.community.api.configuration.VersionedConfig;

public class SettingsManager extends VersionedConfig {
    private static boolean feedFillsSaturation;

    @Override
    protected void reload(Configuration settingsConfig) {
        super.reload(settingsConfig);
        this.feedFillsSaturation = settingsConfig.getBoolean("Feed-Fills-Saturation");
    }

    public static boolean feedFillsSaturation() {
        return feedFillsSaturation;
    }

    @Override
    public double getCurrentConfigVersion() {
        return 1.0;
    }

    @Override
    protected String getConfigFileName() {
        return "settings.yml";
    }
}