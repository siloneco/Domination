package net.azisaba.silo.domination.command;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.azisaba.silo.domination.DominationUtils;
import net.azisaba.silo.domination.game.Game;
import net.azisaba.silo.domination.game.GameHelper;
import net.azisaba.silo.domination.game.GameHelper.GameState;
import net.azisaba.silo.domination.map.MapData;

public class DominationCommand implements CommandExecutor {

    private boolean reloadingMaps = false;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if ( args.length == 0 ) {
            sender.sendMessage(ChatColor.RED + "無効な引数");
            return true;
        }

        if ( args[0].equalsIgnoreCase("generate") ) {

            if ( args.length > 1 ) {
                String mapName = args[1];
                Game game = generateGame(mapName);

                if ( game == null ) {
                    sender.sendMessage(ChatColor.RED + "そのような名前のマップは存在しません");
                    return true;
                }

                generated(game, sender);
                return true;
            }

            Game game = generateGame();

            generated(game, sender);

            return true;
        }

        if ( args[0].equalsIgnoreCase("head") ) {
            ((Player) sender).getInventory().addItem(DominationUtils.getGoldenHead(10));
            return true;
        }

        if ( args[0].equalsIgnoreCase("join") ) {

            if ( !(sender instanceof Player) ) {
                sender.sendMessage(ChatColor.RED + "プレイヤーのみ使用可能です");
                return true;
            }

            Player p = (Player) sender;

            if ( args.length == 2 ) {
                joinGame(p, args[1]);
                return true;
            } else if ( args.length >= 3 ) {
                p = Bukkit.getPlayerExact(args[2]);

                if ( p == null ) {
                    sender.sendMessage(ChatColor.YELLOW + args[2] + ChatColor.RED + "という名前のプレイヤーは見つかりません。");
                    return true;
                }

                joinGame(p, args[1]);
                sender.sendMessage(ChatColor.YELLOW + p.getName() + ChatColor.GREEN + "をゲームに参加させました。");
            } else {
                joinAvailableGame(p);
            }
            return true;
        }

        if ( args[0].equalsIgnoreCase("list") ) {
            sendGameList(sender);
            return true;
        }

        if ( args[0].equalsIgnoreCase("close") ) {

            if ( args.length == 1 ) {
                sender.sendMessage(ChatColor.RED + "ゲームIDを指定してください。");
                return true;
            }

            if ( args[1].equalsIgnoreCase("-this") ) {
                if ( !(sender instanceof Player) ) {
                    sender.sendMessage(ChatColor.RED + "\"this\" は参加しているプレイヤーのみ使用できます");
                    return true;
                }

                Game game = GameHelper.getGame((Player) sender);

                if ( game == null ) {
                    sender.sendMessage(ChatColor.RED + "あなたはゲームに参加していないため \"this\" を指定できません。");
                    return true;
                }

                game.closeGame();
                sender.sendMessage(ChatColor.GREEN + "正常に終了しました。");
                return true;
            }

            Game game = GameHelper.getGame(args[1].toLowerCase());

            if ( game == null ) {
                sender.sendMessage(ChatColor.RED + "そのIDのゲームは存在しません");
                return true;
            }

            game.closeGame();
            sender.sendMessage(ChatColor.GREEN + "正常に終了しました。");
            return true;
        }

        if ( args[0].equalsIgnoreCase("maplist") ) {
            sendMapList(sender);
            return true;
        }

        if ( args[0].equalsIgnoreCase("reloadmap") || args[0].equalsIgnoreCase("reloadmaps") ) {
            reloadMaps(sender);
            return true;
        }
        return true;
    }

    private Game generateGame(String mapName) {
        Game game = GameHelper.generateNewGame(mapName);
        return game;
    }

    private Game generateGame() {
        Game game = GameHelper.generateNewGame();
        return game;
    }

    private void generated(Game game, CommandSender sender) {
        if ( !(sender instanceof Player) ) {
            sender.sendMessage("ゲーム(" + game.getId() + ")を生成しました。");
            return;
        }

        boolean success = game.joinPlayer((Player) sender);

        if ( !success ) {
            sender.sendMessage(ChatColor.RED + "ゲームの参加がキャンセルされました。");
        }
    }

    private void joinAvailableGame(Player p) {
        Game game = GameHelper.getAvailableGame();

        if ( game == null ) {
            game = GameHelper.generateNewGame();

            if ( game != null ) {
                GameHelper.registerGame(game);
            }
        }

        if ( game == null ) {
            GameHelper.getPlugin().getLogger().warning("使用可能なマップがありませんでした。数を増やすなどして対応してください。");

            DominationUtils.sendToLobby(p, ChatColor.RED + "使用可能なマップなし。");
            return;
        }
        boolean success = game.joinPlayer(p);

        if ( !success ) {
            p.sendMessage(ChatColor.RED + "ゲームの参加がキャンセルされました。");
        }
    }

    private void joinGame(Player p, String id) {
        Game game = GameHelper.getGame(id);

        if ( game == null ) {
            p.sendMessage(ChatColor.YELLOW + id + ChatColor.RED + "というIDのゲームは存在しません。");
            return;
        }

        if ( game.getState() == GameState.GAMING || game.getState() == GameState.FINISH ) {
            p.sendMessage(ChatColor.RED + "このゲームはすでに開始済みです。");
            return;
        }
        if ( game.getPlayers().size() >= GameHelper.getPlugin().config.maxPlayers ) {
            p.sendMessage(ChatColor.RED + "人数がいっぱいです。");
            return;
        }

        boolean success = game.joinPlayer(p);

        if ( !success ) {
            p.sendMessage(ChatColor.RED + "ゲームの参加がキャンセルされました。");
        }
    }

    private void sendGameList(CommandSender sender) {

        if ( GameHelper.getAllGames().size() == 0 ) {
            if ( sender instanceof Player ) {
                sender.sendMessage(ChatColor.RED + "ゲームが1つも実行されていません。");
            } else {
                sender.sendMessage("ゲームが1つも実行されていません。");
            }
            return;
        }

        if ( sender instanceof Player ) {
            ((Player) sender).sendMessage(ChatColor.LIGHT_PURPLE + StringUtils.repeat("─", 32));
        } else {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + StringUtils.repeat("─", 32));
        }

        for ( Game game : GameHelper.getAllGames() ) {

            String mapState = (game.getMapGenerator().isFinished() + "").replace("true", ChatColor.GREEN + "Generated")
                    .replace("false", ChatColor.YELLOW + "Generating");

            if ( sender instanceof Player ) {
                ((Player) sender).sendMessage(ChatColor.YELLOW + game.getId() + ChatColor.GREEN + ": " + ChatColor.AQUA
                        + game.getPlayers().size() + " players" + ChatColor.GRAY + " ┃ " + ChatColor.GREEN
                        + game.getData().getMapName() + ChatColor.GRAY + " ┃ " + convertToString(game.getState())
                        + ChatColor.GRAY + " ┃ " + mapState);
            } else {
                sender.sendMessage(
                        game.getId() + ": " + game.getPlayers().size() + " players" + " ┃ "
                                + game.getData().getMapName());
            }
        }

        if ( sender instanceof Player ) {
            ((Player) sender).sendMessage(ChatColor.LIGHT_PURPLE + StringUtils.repeat("─", 32));
        } else {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + StringUtils.repeat("─", 32));
        }
    }

    private String convertToString(GameState state) {
        if ( state == GameState.WAITING ) {
            return ChatColor.GREEN + "Waiting";
        } else if ( state == GameState.COUNTDOWN ) {
            return ChatColor.YELLOW + "Countdown";
        } else if ( state == GameState.GAMING ) {
            return ChatColor.RED + "Gaming";
        } else if ( state == GameState.FINISH ) {
            return ChatColor.DARK_GRAY + "Finished";
        }
        return ChatColor.RESET + "Unknown";
    }

    private void sendMapList(CommandSender sender) {
        List<MapData> mapList = new ArrayList<>(GameHelper.getPlugin().mapSelector.getAllMapData());

        StringBuilder builder = new StringBuilder();
        builder.append(ChatColor.LIGHT_PURPLE + StringUtils.repeat("─", 32) + "\n");
        for ( MapData data : mapList ) {
            builder.append(ChatColor.GRAY + " - " + ChatColor.RED + data.getMapName() + ChatColor.GRAY + " | "
                    + ChatColor.YELLOW + "" + data.getFlags().size() + " flags " + ChatColor.GRAY + " | "
                    + ChatColor.GREEN + "Using in " + ChatColor.YELLOW + GameHelper.getAllGames(data).size() + " "
                    + ChatColor.GREEN + "games." + "\n");
        }

        builder.append(ChatColor.LIGHT_PURPLE + StringUtils.repeat("─", 32));

        sender.sendMessage(new String(builder));
    }

    private void reloadMaps(CommandSender sender) {

        new Thread(() -> {

            if ( reloadingMaps ) {
                sender.sendMessage(ChatColor.RED + "現在再読み込み中です。");
                return;
            }

            reloadingMaps = true;
            sender.sendMessage(ChatColor.GREEN + "非同期でマップを再読み込みします...");

            long start = System.currentTimeMillis();

            List<MapData> dataList = GameHelper.getPlugin().mapLoader.loadAllMapData();
            GameHelper.getPlugin().mapSelector.setMapList(dataList);

            long end = System.currentTimeMillis();

            sender.sendMessage(ChatColor.YELLOW + "完了しました。 " + ChatColor.GRAY + "(" + (end - start) + "ms)");

            reloadingMaps = false;
        }).start();
    }
}
