package net.azisaba.silo.domination.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.azisaba.silo.domination.game.Game;

public class PlayerJoinGameEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled;

    private final Game game;

    private final Player joinedPlayer;

    public PlayerJoinGameEvent(Game game, Player p) {
        this.game = game;
        joinedPlayer = p;
    }

    public Game getGame() {
        return game;
    }

    public Player getJoinedPlayer() {
        return joinedPlayer;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}