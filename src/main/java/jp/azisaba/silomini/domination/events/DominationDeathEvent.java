package jp.azisaba.silomini.domination.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import jp.azisaba.silomini.domination.game.Game;

public class DominationDeathEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Game game;

    private final Player death;
    private final boolean killed;

    public DominationDeathEvent(Game game, Player death, boolean killed) {
        this.game = game;
        this.death = death;
        this.killed = killed;
    }

    public Game getGame() {
        return game;
    }

    public boolean isKilled() {
        return killed;
    }

    public Player getPlayer() {
        return death;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}