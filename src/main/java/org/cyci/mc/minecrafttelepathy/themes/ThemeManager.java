package org.cyci.mc.minecrafttelepathy.themes;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.cyci.mc.minecrafttelepathy.utils.ConfigWrapper;

import java.util.*;

public class ThemeManager {

    private static final Map<String, List<Material>> themes = new HashMap<>();
    private static ConfigWrapper configWrapper;

    public static void initialize(Plugin plugin) {
        configWrapper = new ConfigWrapper(plugin, "themes.yml");
        configWrapper.saveDefaultConfig();
        loadThemesFromConfig();
    }

    private static void loadThemesFromConfig() {
        FileConfiguration config = configWrapper.getConfig();
        for (String themeName : config.getConfigurationSection("themes").getKeys(false)) {
            List<Material> materials = new ArrayList<>();
            for (String materialName : config.getStringList("themes." + themeName)) {
                Material material = Material.getMaterial(materialName);
                if (material != null) {
                    materials.add(material);
                }
            }
            if (!materials.isEmpty()) {
                themes.put(themeName, materials);
            }
        }
    }

    public static Material getRandomBlockForTheme(String themeName) {
        List<Material> materials = themes.getOrDefault(themeName, Collections.singletonList(Material.STONE));
        Random random = new Random();
        return materials.get(random.nextInt(materials.size()));
    }

    public static List<String> getThemeNames() {
        return new ArrayList<>(themes.keySet());
    }
}