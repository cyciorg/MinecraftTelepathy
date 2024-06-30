package org.cyci.mc.minecrafttelepathy.managers;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * org.cyci.mc.minecrafttelepathy.managers
 * --------------------------
 *
 * @author - Phil/DopeDealers
 * @project - MinecraftTelepathy
 * @website - https://cyci.org/projects
 * @email - phillip.kinney@cyci.org
 * @created - Sat - June/Sat/2024
 */
public class TeamManager {

    private final String _color;
    private final String _name;
    private final List<Player> players;
    private int points;

    public TeamManager(String color, String name) {
        this._color = color;
        this._name = name;
        this.players = new ArrayList<>();
        this.points = 0;
    }

    public String getColor() {
        return this._color;
    }

    public String getName() {
        return this._name;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public int getPoints() {
        return points;
    }

    public void addPoints(int points) {
        this.points += points;
    }
}