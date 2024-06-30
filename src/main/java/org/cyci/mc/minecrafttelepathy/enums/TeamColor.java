package org.cyci.mc.minecrafttelepathy.enums;

import java.util.Random;

/**
 * org.cyci.mc.minecrafttelepathy.enums
 * --------------------------
 *
 * @author - Phil/DopeDealers
 * @project - MinecraftTelepathy
 * @website - https://cyci.org/projects
 * @email - phillip.kinney@cyci.org
 * @created - Sun - June/Sun/2024
 */
public enum TeamColor {
    RED(1, "#FF5555Red"), GREEN(2, "#55FF55Green"), BLUE(3, "#5555FFBlue"), YELLOW(4, "#FFFF55Yellow");

    private final Integer _id;
    private final String _coloredName;

    private static final Random RANDOM = new Random();

    TeamColor(Integer id, String coloredName) {
        this._id = id;
        this._coloredName = coloredName;
    }

    public Integer getId() {
        return _id;
    }

    public String getColoredName() {
        return _coloredName;
    }

    public static TeamColor random() {
        TeamColor[] teams = TeamColor.values();
        int x = RANDOM.nextInt(teams.length);
        return TeamColor.class.getEnumConstants()[x];
    }
}

