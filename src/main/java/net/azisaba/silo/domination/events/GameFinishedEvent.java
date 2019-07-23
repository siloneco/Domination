package net.azisaba.silo.domination.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.azisaba.silo.domination.game.Game;
import net.azisaba.silo.domination.teams.DominationTeam;

public class GameFinishedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Game game;
    private final DominationTeam winner;

    public GameFinishedEvent(Game game, DominationTeam winner) {
        this.game = game;
        this.winner = winner;
    }

    public Game getGame() {
        return game;
    }

    public DominationTeam getWinnerTeam() {
        return winner;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}