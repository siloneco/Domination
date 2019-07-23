package net.azisaba.silo.domination.game;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import net.azisaba.silo.domination.DominationUtils;
import net.azisaba.silo.domination.events.DominationDeathEvent;
import net.azisaba.silo.domination.events.GameFinishedEvent;
import net.azisaba.silo.domination.events.GameStartEvent;
import net.azisaba.silo.domination.events.PlayerJoinGameEvent;
import net.azisaba.silo.domination.events.PlayerKillEvent;
import net.azisaba.silo.domination.events.PlayerLeftGameEvent;
import net.azisaba.silo.domination.flag.Flag;
import net.azisaba.silo.domination.flag.FlagAndActionBarUpdater;
import net.azisaba.silo.domination.game.GameHelper.GameState;
import net.azisaba.silo.domination.logger.GameLogger;
import net.azisaba.silo.domination.map.MapData;
import net.azisaba.silo.domination.map.MapGenerator;
import net.azisaba.silo.domination.teams.DominationTeam;
import net.azisaba.silo.domination.teams.TeamManager;
import net.azisaba.silo.domination.timers.CountdownTimer;
import net.azisaba.silo.domination.timers.CountdownType;
import net.azisaba.silo.domination.timers.DominationScoreboard;
import net.azisaba.silo.domination.timers.TeamPointUpdater;
import net.azisaba.silo.domination.timers.WaitingTimer;

public class Game {

    private final String id;
    private final World world;

    private long gameStart = -1;

    private MapData data;
    private GameState state = GameState.WAITING;
    private MapGenerator generator;
    private CountdownTimer ctTimer;
    private FlagAndActionBarUpdater flagUpdater;
    private DominationScoreboard dScoreboard;
    private TeamPointUpdater teamPointUpdater;
    private WaitingTimer waitingTimer;
    private GameLogger logger;

    private final Scoreboard scoreBoard;

    private TeamManager team;

    private int redPoint = 0;
    private int bluePoint = 0;

    private final long createdAt;

    private boolean isClosing = false;

    private DominationTeam winner;

    public Game(String id, World world, MapData data, MapGenerator generator) {
        this.id = id;
        this.world = world;
        this.data = data;
        this.data.setWorld(this.world);
        this.generator = generator;
        scoreBoard = Bukkit.getScoreboardManager().getNewScoreboard();
        dScoreboard = new DominationScoreboard(this);
        waitingTimer = new WaitingTimer(this);
        waitingTimer.runTask();

        char flagName = 'A';
        for ( Flag flag : data.getFlags() ) {
            flag.runDisplayTimer();
            generateFlagArmorStand(flag, flagName);
            flagName++;
        }

        createdAt = System.currentTimeMillis();

        logger = new GameLogger(this);
        logger.writeGameData();
    }

    public List<Player> getPlayers() {
        return world.getPlayers();
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public World getWorld() {
        return world;
    }

    public MapData getData() {
        return data;
    }

    public long getStartedMiliSeconds() {
        return gameStart;
    }

    private int ctSeconds = -1;

    public int getCountdownSeconds() {
        return ctSeconds;
    }

    public void setCountdownSeconds(int seconds) {
        ctSeconds = seconds;
    }

    public TeamManager getTeamManager() {
        return team;
    }

    public int getRedPoint() {
        return redPoint;
    }

    public int getBluePoint() {
        return bluePoint;
    }

    public MapGenerator getMapGenerator() {
        return generator;
    }

    public Scoreboard getScoreboard() {
        return scoreBoard;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void deathPlayer(Player p, boolean killed) {

        p.setHealth(20);
        p.setFoodLevel(20);

        GameHelper.setUpInventory(p, team.getTeam(p));

        if ( team.getTeam(p) == DominationTeam.BLUE ) {
            p.teleport(data.getBlueSpawn());
        } else if ( team.getTeam(p) == DominationTeam.RED ) {
            p.teleport(data.getRedSpawn());
        }

        if ( !killed ) {
            broadcast(getTeamManager().getColor(getTeamManager().getTeam(p)) + p.getName() + ChatColor.GRAY + " died.");
        }

        DominationDeathEvent event = new DominationDeathEvent(this, p, killed);
        Bukkit.getPluginManager().callEvent(event);

        new BukkitRunnable() {
            @Override
            public void run() {
                p.setVelocity(p.getVelocity().multiply(0));
            }
        }.runTaskLater(GameHelper.getPlugin(), 1);
    }

    public boolean killPlayer(Player killer, Player death) {
        if ( killer == death ) {
            return false;
        }

        PlayerKillEvent event = new PlayerKillEvent(this, killer, death);
        Bukkit.getPluginManager().callEvent(event);

        if ( event.isCancelled() ) {
            logger.writeLine(
                    "PlayerKillEvent cancelled. (killer=" + killer.getName() + ", death=" + death.getName() + ")");
            return false;
        }

        broadcast(getTeamManager().getColor(getTeamManager().getTeam(killer)) + killer.getName()
                + ChatColor.GRAY + " killed " + getTeamManager().getColor(getTeamManager().getTeam(death))
                + death.getName() + ChatColor.GRAY + ".");

        logger.killed(killer, death);
        return true;
    }

    public void addTeamPoint(int point, DominationTeam team) {

        if ( state != GameState.GAMING ) {
            return;
        }

        if ( team == DominationTeam.RED ) {
            redPoint += point;
        } else if ( team == DominationTeam.BLUE ) {
            bluePoint += point;
        }

        updateScoreboard();

        if ( redPoint >= GameHelper.getPlugin().config.gameEndPoint
                || bluePoint >= GameHelper.getPlugin().config.gameEndPoint ) {
            endGame();
        }
    }

    public void updateScoreboard() {
        dScoreboard.updateScoreboard();
    }

    public void startGame() {

        GameStartEvent event = new GameStartEvent(this);
        Bukkit.getPluginManager().callEvent(event);

        if ( event.isCancelled() ) {
            return;
        }

        state = GameState.GAMING;

        GameHelper.getPlugin().getLogger().info(id + " が開始");
        gameStart = System.currentTimeMillis();

        team = new TeamManager(this);
        team.initialize();
        team.distribute();

        waitingTimer.stopTask();

        flagUpdater = new FlagAndActionBarUpdater(this);
        flagUpdater.runTask();

        teamPointUpdater = new TeamPointUpdater(this);
        teamPointUpdater.runTask();

        for ( Player p : team.getRedPlayers() ) {
            p.teleport(data.getRedSpawn());
            p.sendMessage(ChatColor.YELLOW + "あなたは" + ChatColor.RED + "赤チーム" + ChatColor.YELLOW + "になりました!");

            DominationUtils.sendTitle(p, ChatColor.YELLOW + "試合開始!!",
                    ChatColor.RED + "赤チーム" + ChatColor.YELLOW + "に選ばれました!", 0, 30, 10);
            p.playSound(p.getLocation(), Sound.ANVIL_LAND, 1, 1);

            GameHelper.setUpInventory(p, DominationTeam.RED);
        }

        for ( Player p : team.getBluePlayers() ) {
            p.teleport(data.getBlueSpawn());
            p.sendMessage(ChatColor.YELLOW + "あなたは" + ChatColor.BLUE + "青チーム" + ChatColor.YELLOW + "になりました!");

            DominationUtils.sendTitle(p, ChatColor.YELLOW + "試合開始!!",
                    ChatColor.BLUE + "青チーム" + ChatColor.YELLOW + "に選ばれました!", 0, 30, 10);
            p.playSound(p.getLocation(), Sound.ANVIL_LAND, 1, 1);

            GameHelper.setUpInventory(p, DominationTeam.BLUE);
        }

        for ( Player p : world.getPlayers() ) {
            p.sendMessage(ChatColor.YELLOW + "試合開始!");

            p.setFlying(false);
            p.setAllowFlight(false);

            p.setGameMode(GameMode.SURVIVAL);
        }

        ctTimer.stopTimer();
        ctTimer = new CountdownTimer(GameHelper.getPlugin(), this, CountdownType.GAMING);
        ctTimer.runTimer();

        dScoreboard.updateScoreboard();

        logger.startGame();
    }

    public void endGame() {

        state = GameState.FINISH;
        ctTimer.stopTimer();

        teamPointUpdater.stopTask();

        data.getFlags().stream().forEach(flag -> {
            flag.setDisable(true);
        });

        GameHelper.getPlugin().getLogger().info(id + " のゲームが終了");

        String winnerAnnounce = "";

        if ( redPoint >= GameHelper.getPlugin().config.gameEndPoint ) {
            winnerAnnounce = ChatColor.RED + "赤チーム" + ChatColor.YELLOW + "が勝利しました!";

            winner = DominationTeam.RED;
        } else if ( bluePoint >= GameHelper.getPlugin().config.gameEndPoint ) {
            winnerAnnounce = ChatColor.BLUE + "青チーム" + ChatColor.YELLOW + "が勝利しました!";

            winner = DominationTeam.BLUE;
        } else if ( (System.currentTimeMillis() - gameStart) / 1000 > GameHelper.getPlugin().config.gameLongSecond ) {

            if ( redPoint > bluePoint ) {
                winnerAnnounce = ChatColor.RED + "赤チーム" + ChatColor.YELLOW + "が勝利しました!";

                winner = DominationTeam.RED;
            } else if ( redPoint < bluePoint ) {
                winnerAnnounce = ChatColor.BLUE + "青チーム" + ChatColor.YELLOW + "が勝利しました!";

                winner = DominationTeam.BLUE;
            } else {
                winnerAnnounce = ChatColor.YELLOW + "引き分け";

                winner = DominationTeam.NONE;
            }
        }

        logger.writeLines(Arrays.asList(StringUtils.repeat("-", 32), "勝者: " + winner.toString(), "チームポイント:",
                " - 赤: " + redPoint + " pt", " - 青: " + bluePoint + " pt",
                StringUtils.repeat("-", 32)));

        for ( Player p : world.getPlayers() ) {
            p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 1);
            DominationUtils.sendActionBar(p, winnerAnnounce);
        }

        broadcast(ChatColor.GREEN + "試合終了! " + winnerAnnounce);
        broadcast(ChatColor.GRAY + "10秒後にゲームを終了します...");

        dScoreboard.updateScoreboard();

        Bukkit.getScheduler().runTaskLater(GameHelper.getPlugin(), () -> {

            world.getPlayers().forEach(p1 -> {
                GameHelper.joinAvailableGame(p1);
            });

            World world = Bukkit.getWorld("world");

            world.getPlayers().stream().forEach(p2 -> {
                if ( world != null ) {
                    p2.teleport(world.getSpawnLocation());
                }
            });

            closeGame();
        }, 20 * 10);

        GameFinishedEvent event = new GameFinishedEvent(this, winner);
        Bukkit.getPluginManager().callEvent(event);
    }

    public void runStartCountdown() {

        ctTimer = new CountdownTimer(GameHelper.getPlugin(), this, CountdownType.START);
        ctTimer.runTimer();

        state = GameState.COUNTDOWN;

        dScoreboard.updateScoreboard();
    }

    public void closeGame() {

        if ( isClosing ) {
            return;
        }

        isClosing = true;

        logger.writeLine("Closing Game...");

        long start = System.currentTimeMillis();

        if ( Bukkit.getScoreboardManager().getMainScoreboard().getObjective(id + "_side") != null ) {
            Bukkit.getScoreboardManager().getMainScoreboard().getObjective(id + "_side").unregister();
        }
        if ( generator != null ) {
            generator.stopTimer();
        }
        if ( ctTimer != null ) {
            ctTimer.stopTimer();
        }
        if ( flagUpdater != null ) {
            flagUpdater.stopTask();
        }
        if ( teamPointUpdater != null ) {
            teamPointUpdater.stopTask();
        }
        if ( waitingTimer != null ) {
            waitingTimer.stopTask();
        }

        for ( Flag flag : data.getFlags() ) {
            flag.stopDisplayTimer();
        }

        for ( Player p : world.getPlayers() ) {
            p.teleport(Bukkit.getWorld("world").getSpawnLocation());
        }
        if ( generator != null ) {
            generator.stopTimer();
        }
        if ( team != null ) {
            team.removeTeam();
        }

        releaseMemory();
        deleteFiles();

        GameHelper.deleteGame(getId());

        long end = System.currentTimeMillis();
        Bukkit.getLogger().info("Closed game(" + id + "). " + (end - start) + "ms.");

        logger.writeLine("Closed Game. (" + (end - start) + "ms)");
        logger.writeLine("---------------- EOF ----------------");

        logger = null;

        if ( GameHelper.getAllGames().size() < GameHelper.getPlugin().config.standByGameCount ) {
            Bukkit.getLogger().info("Creating new game...");
            GameHelper.generateNewGame();
        }
    }

    public boolean joinPlayer(Player p) {

        if ( GameHelper.getGame(p) == this ) {
            p.sendMessage(ChatColor.RED + "あなたはすでにゲームに参加しています。");
            return false;
        }

        PlayerJoinGameEvent event = new PlayerJoinGameEvent(this, p);
        Bukkit.getPluginManager().callEvent(event);

        if ( event.isCancelled() ) {
            logger.writeLine("JoinEvent cancelled (Player=" + p.getName() + ")");
            return false;
        }

        p.teleport(data.getGeneralSpawn());

        String joinMsg = DominationUtils.getPrefixColor(p) + p.getName() + ChatColor.AQUA + " joined the game.";

        for ( Player target : world.getPlayers() ) {
            target.sendMessage(joinMsg);
        }

        if ( !world.getPlayers().contains(p) ) {
            p.sendMessage(joinMsg);
        }

        p.setGameMode(GameMode.SURVIVAL);

        p.getInventory().clear();
        p.getInventory().setArmorContents(new ItemStack[4]);

        p.setAllowFlight(true);
        p.setFlying(true);

        int playerCount = world.getPlayers().size();
        if ( !world.getPlayers().contains(p) ) {
            playerCount++;
        }

        if ( playerCount >= GameHelper.getPlugin().config.minPlayers && state == GameState.WAITING
                && generator.isFinished() ) {
            runStartCountdown();
        }

        dScoreboard.updateScoreboard();

        logger.joinedPlayer(p);
        GameHelper.getPlugin().getLogger().info(p.getName() + " joined game id " + id + ".");

        return true;
    }

    public void quitPlayer(Player p) {

        if ( state != GameState.FINISH ) {
            String leftMsg = DominationUtils.getPrefixColor(p) + p.getName() + ChatColor.AQUA + " left the game.";

            for ( Player target : world.getPlayers() ) {
                target.sendMessage(leftMsg);
            }

            if ( !world.getPlayers().contains(p) ) {
                p.sendMessage(leftMsg);
            }
        }

        if ( team != null ) {
            team.quit(p);
        }

        if ( state == GameState.COUNTDOWN ) {
            int playerCount = world.getPlayers().size();
            if ( world.getPlayers().contains(p) ) {
                playerCount -= 1;
            }

            if ( playerCount < GameHelper.getPlugin().config.minPlayers ) {
                GameHelper.getPlugin().getLogger().info(id + " の開始カウントダウンを中断");
                logger.writeLine("Countdown canncelled.");

                ctTimer.stopTimer();
                state = GameState.WAITING;

                for ( Player target : world.getPlayers() ) {
                    if ( target == p ) {
                        continue;
                    }

                    target.sendMessage(ChatColor.RED + "人数が足りないためカウントダウンをキャンセルしました。");
                    target.playSound(target.getLocation(), Sound.NOTE_BASS, 1, 1);
                }
            }
        } else if ( state == GameState.GAMING ) {

            if ( team.getRedPlayers().size() == 0 || team.getBluePlayers().size() == 0 ) {
                for ( Player target : world.getPlayers() ) {
                    DominationUtils.sendToLobby(target, ChatColor.RED + "人数が足りないためゲームを終了します...");
                }

                closeGame();
            }
        }

        logger.leftPlayer(p.getName());

        PlayerLeftGameEvent event = new PlayerLeftGameEvent(this, p);
        Bukkit.getPluginManager().callEvent(event);
    }

    public DominationTeam getWinnerTeam() {
        return winner;
    }

    public void broadcast(String msg) {
        for ( Player p : world.getPlayers() ) {
            p.sendMessage(msg);
        }

        logger.writeLine("[Broadcast]: " + msg);
    }

    public GameLogger getLogger() {
        return logger;
    }

    private void deleteFiles() {

        logger.writeLine("Deleting world...");
        boolean success = Bukkit.unloadWorld(world, false);

        if ( success ) {

            logger.writeLine("World unloaded successfully.");
            logger.writeLine("Deleting folder...");

            File file = world.getWorldFolder();
            try {
                FileUtils.deleteDirectory(file);

                logger.writeLine("Deleted successfully.");
            } catch ( IOException e ) {

                logger.writeLine("Failed to delete world folder.");
                logger.writeError(e.getStackTrace());

                e.printStackTrace();
            }

            file = new File("./plugins/WorldGuard/worlds/" + getId());

            if ( file.exists() ) {
                try {
                    FileUtils.deleteDirectory(file);
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            }
        } else {
            GameHelper.getPlugin().getLogger().warning(world.getName() + "のunloadに失敗");
        }
    }

    private void releaseMemory() {
        data = null;
        generator = null;
        ctTimer = null;
        flagUpdater = null;
        dScoreboard = null;
        teamPointUpdater = null;
        waitingTimer = null;
        team = null;
        redPoint = 0;
        bluePoint = 0;
    }

    private void generateFlagArmorStand(Flag flag, char flagName) {
        new BukkitRunnable() {
            @Override
            public void run() {
                flag.spawnArmorStand();
                flag.setFlagChar(flagName);
            }
        }.runTaskLater(GameHelper.getPlugin(), 20);
    }
}
