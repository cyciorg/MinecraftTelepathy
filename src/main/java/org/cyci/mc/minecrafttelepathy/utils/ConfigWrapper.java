package org.cyci.mc.minecrafttelepathy.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigWrapper {
    private final Plugin plugin;
    private final String fileName;
    private File configFile;
    private FileConfiguration config;

    private long lastModified = 0;

    public ConfigWrapper(Plugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.configFile = new File(plugin.getDataFolder(), fileName);
    }

    public String getFileName() {
        return (config != null && configFile != null) ? configFile.getName() : null;
    }

    public File getConfigFile() {
        return (config != null && configFile != null) ? configFile.getAbsoluteFile() : null;
    }

    public void loadConfig(String header) {
        if (config == null) {
            reload();
        }
        config.options().getHeader().add(header);
        config.options().copyDefaults(true);
        saveConfig();
    }

    public void updateCachedConfig() {
        if (configFile != null) {
            config = YamlConfiguration.loadConfiguration(configFile);
        }
    }

    public void reloadConfig() {
        updateCachedConfig();
        final InputStream defConfigStream = plugin.getResource(fileName);
        if (defConfigStream != null) {
            config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)));
        }
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
        final InputStream defConfigStream = plugin.getResource(fileName);
        if (defConfigStream != null) {
            config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)));
        }
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            reload();
        }
        return config;
    }

    public void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }
        try {
            getConfig().save(configFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save " + fileName);
        }
    }

    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            plugin.saveResource(fileName, false);
        }
    }
}