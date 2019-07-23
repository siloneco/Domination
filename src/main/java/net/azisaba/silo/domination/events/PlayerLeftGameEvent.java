package net.azisaba.silo.domination.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.azisaba.silo.domination.game.Game;

public class PlayerLeftGameEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Game game;

    private final Player quitPlayer;

    public PlayerLeftGameEvent(Game game, Player p) {
        this.game = game;
        quitPlayer = p;
    }

    public Game getGame() {
        return game;
    }

    public Player getQuitPlayer() {
        return quitPlayer;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}