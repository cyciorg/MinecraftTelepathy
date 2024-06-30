package org.cyci.mc.minecrafttelepathy.enums;

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

    TeamColor(Integer id, String coloredName) {
        this._id = id;
        this._coloredName = coloredName;
    }

    public static TeamColor random() {

    }
}

