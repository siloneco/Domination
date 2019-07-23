package net.azisaba.silo.domination.map;

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;

import net.azisaba.silo.domination.flag.Flag;
import net.azisaba.silo.domination.game.GameHelper;

public class MapImportHelper {

    private static HashMap<Player, MapImporter> importerMap = new HashMap<>();

    public static MapImporter getImporter(Player p) {
        if ( !importerMap.containsKey(p) ) {
            return null;
        }

        return importerMap.get(p);
    }

    public static void setImporter(Player p, MapImporter importer) {
        importerMap.put(p, importer);
    }

    public static boolean isImportMode(Player p) {
        return importerMap.containsKey(p);
    }

    public static void disableImportMode(Player p) {

        if ( importerMap.containsKey(p) ) {

            importerMap.get(p).stopAllFlagTimers();

            importerMap.remove(p);
        }

        p.getInventory().clear();
    }

    public static void enableImportMode(Player p, String mapName, boolean loadFromExists) {

        if ( loadFromExists ) {
            MapData data = GameHelper.getPlugin().mapSelector.getMap(mapName);

            if ( data == null ) {
                p.sendMessage(ChatColor.RED + "マップが存在しません。-loadを消して実行してください。");
                return;
            }

            p.setGameMode(GameMode.CREATIVE);
            setImportInventory(p);

            MapImporter importer = new MapImporter(p, mapName);

            data.setWorld(p.getWorld());

            for ( Flag flag : data.getFlags() ) {

                Location loc = flag.getLocation().clone();
                loc.setWorld(p.getWorld());
                loc.add(0, data.getLoweredY(), 0);

                importer.addFlag(new Flag(loc));
            }

            Location blue = data.getBlueSpawn();
            Location red = data.getRedSpawn();
            Location general = data.getGeneralSpawn();
            blue.add(0, data.getLoweredY(), 0);
            red.add(0, data.getLoweredY(), 0);
            general.add(0, data.getLoweredY(), 0);

            importer.setBlueSpawn(blue);
            importer.setRedSpawn(red);
            importer.setGeneralSpawn(general);

            importerMap.put(p, importer);

            p.sendMessage(ChatColor.RED + mapName + ChatColor.GREEN + " のデータをロードしました。(範囲指定のロードはされていません。)");
            return;
        }

        p.setGameMode(GameMode.CREATIVE);
        setImportInventory(p);

        importerMap.put(p, new MapImporter(p, mapName));

        p.sendMessage(ChatColor.GREEN + "マップ名 " + ChatColor.RED + mapName + ChatColor.GREEN + " でImportモードを起動しました。");
    }

    private static void setImportInventory(Player p) {
        p.getInventory().clear();
        p.getInventory().setItem(0, new ItemStack(Material.COMPASS));
        p.getInventory().setItem(1, getWhiteWool());
        p.getInventory().setItem(2, getRedWool());
        p.getInventory().setItem(3, getBlueWool());
        p.getInventory().setItem(6, getIronAxe());
        p.getInventory().setItem(7, getBanner());
        p.getInventory().setItem(8, getAcceptImportItem());
    }

    private static ItemStack getRedWool() {
        ItemStack item = new ItemStack(Material.WOOL, 1, (byte) 14);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.RED + "赤のスポーン地点");
        meta.setLore(Arrays.asList(ChatColor.YELLOW + "設定したい場所でアイテムをクリック"));

        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack getWhiteWool() {
        ItemStack item = new ItemStack(Material.WOOL);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.YELLOW + "初期スポーン地点");
        meta.setLore(Arrays.asList(ChatColor.YELLOW + "設定したい場所でアイテムをクリック"));

        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack getBlueWool() {
        ItemStack item = new ItemStack(Material.WOOL, 1, (byte) 11);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.AQUA + "青のスポーン地点");
        meta.setLore(Arrays.asList(ChatColor.YELLOW + "設定したい場所でアイテムをクリック"));

        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack getIronAxe() {
        ItemStack item = new ItemStack(Material.IRON_AXE);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GREEN + "インポート範囲選択");
        meta.setLore(Arrays.asList(ChatColor.YELLOW + "左で最小、右で最大を指定"));

        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack getBanner() {
        ItemStack item = new ItemStack(Material.BANNER);
        BannerMeta meta = (BannerMeta) item.getItemMeta();

        meta.setBaseColor(DyeColor.WHITE);

        meta.setDisplayName(ChatColor.GREEN + "旗");
        meta.setLore(Arrays.asList(ChatColor.YELLOW + "ブロックに設置して使用"));

        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack getAcceptImportItem() {
        ItemStack item = new ItemStack(Material.STAINED_CLAY, 1, (byte) 5);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GREEN + "クリックでインポート開始");

        item.setItemMeta(meta);
        return item;
    }
}
