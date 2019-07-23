package net.azisaba.silo.domination.listeners;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;

import net.azisaba.silo.domination.flag.Flag;
import net.azisaba.silo.domination.game.GameHelper;
import net.azisaba.silo.domination.map.MapData;
import net.azisaba.silo.domination.map.MapImportHelper;
import net.azisaba.silo.domination.map.MapImporter;

public class ImportListener implements Listener {

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent e) {
        Player p = e.getPlayer();

        if ( !MapImportHelper.isImportMode(p) ) {
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if ( !MapImportHelper.isImportMode(p) ) {
            return;
        }

        MapImporter importer = MapImportHelper.getImporter(p);
        ItemStack item = e.getItem();

        Location pLoc = p.getLocation().clone();
        pLoc = roundLocation(pLoc);

        if ( getRedWool().equals(item) ) {
            importer.setRedSpawn(pLoc);
            p.sendMessage(
                    ChatColor.RED + "赤のスポーン地点を" + ChatColor.GREEN + getCordinate(pLoc) + ChatColor.RED + "に変更しました");
        } else if ( getBlueWool().equals(item) ) {
            importer.setBlueSpawn(pLoc);
            p.sendMessage(
                    ChatColor.AQUA + "青のスポーン地点を" + ChatColor.GREEN + getCordinate(pLoc) + ChatColor.AQUA + "に変更しました");
        } else if ( getWhiteWool().equals(item) ) {
            importer.setGeneralSpawn(pLoc);
            p.sendMessage(
                    ChatColor.YELLOW + "デフォルトのスポーン地点を" + ChatColor.GREEN + getCordinate(pLoc) + ChatColor.YELLOW
                            + "に変更しました");
        } else if ( getAcceptImportItem().equals(item) ) {

            String str = importer.getFailImportMessage();

            if ( !str.equals("") ) {
                p.sendMessage(ChatColor.RED + "インポートできません。 理由: " + ChatColor.RESET + str);
            } else {
                new Thread(() -> {
                    p.sendMessage(ChatColor.GREEN + "インポートしています...");
                    MapData data = importer.importData();
                    GameHelper.getPlugin().mapLoader.saveMapData(data);
                    GameHelper.getPlugin().mapSelector.addMap(data);
                    p.sendMessage(ChatColor.GREEN + "マップ名" + ChatColor.GREEN + data.getMapName() + ChatColor.GREEN
                            + "で正常に保存しました。" + ChatColor.GRAY + "(再起動は不要です)");
                }).start();
            }
        } else {
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void setAreaListener(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if ( !MapImportHelper.isImportMode(p) ) {
            return;
        }

        if ( !e.getAction().toString().endsWith("CLICK_BLOCK") ) {
            return;
        }

        MapImporter importer = MapImportHelper.getImporter(p);
        Block b = e.getClickedBlock();

        if ( !getIronAxe().equals(e.getItem()) ) {
            return;
        }

        e.setCancelled(true);

        if ( e.getAction() == Action.LEFT_CLICK_BLOCK ) {
            importer.setMinimum(b.getLocation().clone());

            int size = importer.countAreaBlocks();
            String cordinate = p.getLocation().getBlockX() + "," + p.getLocation().getBlockY() + ","
                    + p.getLocation().getBlockZ();
            if ( size == -1 ) {
                p.sendMessage(ChatColor.YELLOW + "最小ポイントを設定 (" + ChatColor.GREEN + cordinate + ChatColor.YELLOW + ")");
            } else {
                p.sendMessage(ChatColor.YELLOW + "最小ポイントを設定 (" + ChatColor.GREEN + cordinate + ChatColor.YELLOW + ") "
                        + ChatColor.GRAY + "(" + size + ")");
            }
        } else {
            importer.setMaximum(b.getLocation().clone());

            int size = importer.countAreaBlocks();
            String cordinate = p.getLocation().getBlockX() + "," + p.getLocation().getBlockY() + ","
                    + p.getLocation().getBlockZ();
            if ( size == -1 ) {
                p.sendMessage(ChatColor.YELLOW + "最大ポイントを設定 (" + ChatColor.GREEN + cordinate + ChatColor.YELLOW + ")");
            } else {
                p.sendMessage(ChatColor.YELLOW + "最大ポイントを設定 (" + ChatColor.GREEN + cordinate + ChatColor.YELLOW + ") "
                        + ChatColor.GRAY + "(" + size + ")");
            }
        }
    }

    @EventHandler
    public void addFlagListener(BlockPlaceEvent e) {
        Player p = e.getPlayer();

        ItemStack item = e.getItemInHand();

        if ( !getBanner().equals(item) ) {
            return;
        }
        if ( !MapImportHelper.isImportMode(p) ) {
            return;
        }

        MapImporter importer = MapImportHelper.getImporter(p);

        e.setCancelled(true);
        Location loc = e.getBlock().getLocation().clone();
        loc.add(0.5, 0, 0.5);

        importer.addFlag(new Flag(loc));

        p.sendMessage(ChatColor.GREEN + "旗を追加しました。");
    }

    private ItemStack getRedWool() {
        ItemStack item = new ItemStack(Material.WOOL, 1, (byte) 14);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.RED + "赤のスポーン地点");
        meta.setLore(Arrays.asList(ChatColor.YELLOW + "設定したい場所でアイテムをクリック"));

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack getWhiteWool() {
        ItemStack item = new ItemStack(Material.WOOL);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.YELLOW + "初期スポーン地点");
        meta.setLore(Arrays.asList(ChatColor.YELLOW + "設定したい場所でアイテムをクリック"));

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack getBlueWool() {
        ItemStack item = new ItemStack(Material.WOOL, 1, (byte) 11);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.AQUA + "青のスポーン地点");
        meta.setLore(Arrays.asList(ChatColor.YELLOW + "設定したい場所でアイテムをクリック"));

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack getIronAxe() {
        ItemStack item = new ItemStack(Material.IRON_AXE);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GREEN + "インポート範囲選択");
        meta.setLore(Arrays.asList(ChatColor.YELLOW + "左で最小、右で最大を指定"));

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack getBanner() {
        ItemStack item = new ItemStack(Material.BANNER);
        BannerMeta meta = (BannerMeta) item.getItemMeta();

        meta.setBaseColor(DyeColor.WHITE);

        meta.setDisplayName(ChatColor.GREEN + "旗");
        meta.setLore(Arrays.asList(ChatColor.YELLOW + "ブロックに設置して使用"));

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack getAcceptImportItem() {
        ItemStack item = new ItemStack(Material.STAINED_CLAY, 1, (byte) 5);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GREEN + "クリックでインポート開始");

        item.setItemMeta(meta);
        return item;
    }

    private Location roundLocation(Location loc) {
        if ( loc == null ) {
            return null;
        }

        loc.setX(Double.parseDouble(String.format("%.1f", loc.getX())));
        loc.setY(Double.parseDouble(String.format("%.1f", loc.getY())));
        loc.setZ(Double.parseDouble(String.format("%.1f", loc.getZ())));

        loc.setPitch(Float.parseFloat(String.format("%.2f", loc.getPitch())));
        loc.setYaw(Float.parseFloat(String.format("%.2f", loc.getYaw())));

        return loc;
    }

    private String getCordinate(Location loc) {
        String cordinate = ChatColor.GREEN + "" + loc.getX() + "," + loc.getY() + "," + loc.getZ() + ","
                + loc.getYaw() + "," + loc.getPitch();
        cordinate = cordinate.replace(",", ChatColor.RED + "," + ChatColor.GREEN);
        return cordinate;
    }
}