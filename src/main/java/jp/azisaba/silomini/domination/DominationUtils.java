package jp.azisaba.silomini.domination;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;

import jp.azisaba.silomini.domination.game.GameHelper;
import jp.azisaba.silomini.domination.teams.DominationTeam;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LocalizedNode;
import me.lucko.luckperms.api.User;

public class DominationUtils {

    private static Domination plugin;

    protected static void init(Domination plugin) {
        DominationUtils.plugin = plugin;
    }

    public static String getPrefixColor(Player p) {

        try {
            String prefix = getPrefix(p);
            prefix = prefix.substring(prefix.length() - 2);
            return prefix;
        } catch ( Exception e ) {
            return "";
        }
    }

    public static String getPrefix(Player p) {
        User user = LuckPerms.getApi().getUser(p.getName());

        String prefix = "";
        for ( LocalizedNode node : user.getAllNodes() ) {

            List<String> prefixList = new ArrayList<>();
            for ( String perm : node.getNode().getPermission().split("\n") ) {
                if ( perm.startsWith("prefix.") ) {
                    prefixList.add(perm);
                }
            }

            if ( prefixList.size() == 0 ) {
                continue;
            }

            String[] strs = prefixList.get(prefixList.size() - 1).split("\\.");
            prefix = ChatColor.translateAlternateColorCodes('&', strs[strs.length - 1]);
        }

        return prefix;
    }

    public static String getMark() {

        int milli = Calendar.getInstance().get(Calendar.MILLISECOND);

        if ( milli < 250 ) {
            return "┃";
        } else if ( milli < 500 ) {
            return "＼";
        } else if ( milli < 750 ) {
            return "─";
        } else {
            return "／";
        }
    }

    public static void sendActionBar(Player player, String msg) {
        PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"), (byte) 2);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    public static void sendTitle(Player p, String main, String sub, int fadein, int stay, int fadeout) {
        IChatBaseComponent mainTitle = null;
        IChatBaseComponent subTitle = null;
        if ( main != null ) {
            mainTitle = ChatSerializer.a("{\"text\": \"" + main + "\",color:RESET}");
        }
        if ( sub != null ) {
            subTitle = ChatSerializer.a("{\"text\": \"" + sub + "\",color:RESET}");
        }

        if ( mainTitle != null ) {
            PacketPlayOutTitle title = new PacketPlayOutTitle(EnumTitleAction.TITLE, mainTitle);
            PacketPlayOutTitle length = new PacketPlayOutTitle(fadein, stay, fadeout);

            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(title);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(length);
        }
        if ( subTitle != null ) {
            PacketPlayOutTitle title = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, subTitle);
            PacketPlayOutTitle length = new PacketPlayOutTitle(fadein, stay, fadeout);

            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(title);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(length);
        }
    }

    public static ChatColor getColor(DominationTeam team) {
        if ( team == DominationTeam.RED ) {
            return ChatColor.RED;
        } else if ( team == DominationTeam.BLUE ) {
            return ChatColor.BLUE;
        }
        return ChatColor.WHITE;
    }

    public static String generateBar(int num, ChatColor color) {
        return color + StringUtils.repeat("┃", num) + ChatColor.WHITE + StringUtils.repeat("┃", 100 - num);
    }

    public static void sendToLobby(Player p, String msg) {

        p.teleport(Bukkit.getWorld("world").getSpawnLocation());

        new BukkitRunnable() {
            @Override
            public void run() {
                if ( Bukkit.getPluginManager().getPlugin("LobbySender") != null ) {
                    sendPlayer(p, "lobby");
                    p.sendMessage(msg);
                }
            }
        }.runTaskLater(GameHelper.getPlugin(), 0);
    }

    private static ItemStack head;

    public static ItemStack getGoldenHead(int amount) {

        if ( head != null ) {
            ItemStack item = head.clone();
            item.setAmount(amount);
            return item;
        }

        String url = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv"
                + "NGFiZDcwM2U1YjhjODhkNGIxZmNmYTk0YTkzNmEwZDZhNGY2YWJhNDQ1Njk2NjNkMzM5MWQ0ODgzMjIzYzUifX19";

        ItemStack item = getSkull(url);

        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GOLD + "Golden Head");
        meta.setLore(Arrays.asList(ChatColor.YELLOW + "右クリックで食べる"));

        item.setItemMeta(meta);

        head = item.clone();

        item.setAmount(amount);
        return item;
    }

    public static boolean isGoldenHead(ItemStack item) {
        if ( item == null ) {
            return false;
        }
        if ( !item.hasItemMeta() ) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        ItemStack head = getGoldenHead(1);

        if ( !meta.hasDisplayName() ) {
            return false;
        }
        if ( !meta.getDisplayName().equalsIgnoreCase(head.getItemMeta().getDisplayName()) ) {
            return false;
        }

        if ( !meta.hasLore() ) {
            return false;
        }
        if ( meta.getLore().size() != head.getItemMeta().getLore().size() ) {
            return false;
        }

        return true;
    }

    private static ItemStack getSkull(String url) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);

        if ( url.isEmpty() ) {
            return head;
        }

        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);

        profile.getProperties().put("textures", new Property("textures", url));

        try {
            Field profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);

        } catch ( IllegalArgumentException | NoSuchFieldException | SecurityException | IllegalAccessException error ) {
            error.printStackTrace();
        }
        head.setItemMeta(headMeta);
        return head;
    }

    private static void sendPlayer(Player p, String serverName) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("Connect");
            out.writeUTF(serverName);
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }

        p.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
    }
}
