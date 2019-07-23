package net.azisaba.silo.domination.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.azisaba.silo.domination.Domination;

public class MapGenerator {

    private final Domination plugin;

    private final World world;
    private final MapData mapdata;

    private HashMap<String, Material> materialMap = new HashMap<>();
    private HashMap<String, Byte> dataMap = new HashMap<>();

    private List<String> sortedLocStrList = new ArrayList<>();

    private BukkitTask task;

    private boolean finished = false;

    public MapGenerator(Domination plugin, World world, MapData mapdata) {
        this.plugin = plugin;
        this.world = world;
        this.mapdata = mapdata;

        sortedLocStrList = mapdata.getSortedLocationList();
    }

    private int generated = 0;

    @SuppressWarnings("unchecked")
    public void runTimer() {

        materialMap = (HashMap<String, Material>) mapdata.getMaterialMap().clone();
        dataMap = (HashMap<String, Byte>) mapdata.getDataMap().clone();

        task = new BukkitRunnable() {

            private final int stopMilliSeconds = plugin.config.generatorStopMilliSeconds;

            private final List<Location> torchLocations = new ArrayList<>();

            @Override
            @SuppressWarnings("deprecation")
            public void run() {

                if ( sortedLocStrList.size() == 0 && torchLocations.size() != 0 ) {

                    for ( Location loc : torchLocations ) {
                        Block block = loc.getBlock();
                        BlockFace face = BlockFace.UP;

                        for ( BlockFace checkFace : Arrays.asList(BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST,
                                BlockFace.SOUTH, BlockFace.WEST) ) {
                            if ( block.getRelative(checkFace).getType().isOccluding()
                                    && block.getRelative(checkFace).getType().isSolid() ) {
                                face = checkFace;
                                break;
                            }
                        }

                        byte data;

                        switch (face) {
                        case EAST:
                            data = 0x2;
                            break;
                        case WEST:
                            data = 0x1;
                            break;
                        case SOUTH:
                            data = 0x4;
                            break;
                        case NORTH:
                            data = 0x3;
                            break;
                        case UP:
                        default:
                            data = 0x5;
                        }

                        CraftBlock obcBlock = (CraftBlock) block;
                        obcBlock.setTypeIdAndData(Material.TORCH.getId(), data, false);
                    }

                    torchLocations.clear();
                }

                if ( sortedLocStrList.size() == 0 ) {

                    for ( Player p : world.getPlayers() ) {
                        p.sendMessage(ChatColor.GREEN + "生成終了");
                    }

                    releaseMemory();

                    cancel();
                    finished = true;
                    return;
                }

                List<String> locList = new ArrayList<>();
                locList.clear();

                long start = System.currentTimeMillis();
                for ( String locStr : sortedLocStrList ) {

                    if ( System.currentTimeMillis() - start >= stopMilliSeconds ) {
                        break;
                    }

                    Location loc = parseLocation(locStr);

                    locList.add(locStr);

                    Location worldLoc = loc.clone();
                    worldLoc.setWorld(world);

                    if ( worldLoc.getBlock().getBiome() != Biome.PLAINS ) {
                        worldLoc.getBlock().setBiome(Biome.PLAINS);
                    }

                    if ( materialMap.get(locStr) == Material.TORCH ) {
                        torchLocations.add(worldLoc);
                        generated++;
                        continue;
                    }

                    worldLoc.getBlock().setType(materialMap.get(locStr), true);

                    if ( dataMap.containsKey(locStr) ) {
                        worldLoc.getBlock().setData(dataMap.get(locStr));
                    }

                    generated++;
                }

                sortedLocStrList = sortedLocStrList.subList(generated, sortedLocStrList.size());
                generated = 0;
            }
        }.runTaskTimer(plugin, 0, plugin.config.generatorTimerTicks);
    }

    public void stopTimer() {
        if ( task != null ) {
            task.cancel();
        }
    }

    public int finishedPercentage() {
        if ( materialMap == null || sortedLocStrList == null || dataMap == null ) {
            return 100;
        }

        return (materialMap.keySet().size() - sortedLocStrList.size()) * 100 / materialMap.keySet().size();
    }

    public boolean isFinished() {
        return finished;
    }

    @SuppressWarnings("deprecation")
    public void setBiome(Biome biome) {
        for ( String locStr : materialMap.keySet() ) {
            Location worldLoc = parseLocation(locStr).clone();
            worldLoc.setWorld(world);

            if ( worldLoc.getBlock().getBiome() != biome ) {
                worldLoc.getBlock().setBiome(biome);
            }
        }

        List<Chunk> refreshed = new ArrayList<>();

        for ( String locStr : materialMap.keySet() ) {
            Location worldLoc = parseLocation(locStr).clone();
            worldLoc.setWorld(world);

            if ( !refreshed.contains(worldLoc.getChunk()) ) {
                world.refreshChunk(worldLoc.getChunk().getX(), worldLoc.getChunk().getZ());
                refreshed.add(worldLoc.getChunk());
            }
        }
    }

    private Location parseLocation(String str) {
        Location loc = null;
        try {

            String[] strings = str.split(",");

            loc = new Location(null, Double.parseDouble(strings[0]),
                    Double.parseDouble(strings[1]), Double.parseDouble(strings[2]));
            loc.setYaw(Float.parseFloat(strings[3]));
            loc.setPitch(Float.parseFloat(strings[4]));
        } catch ( Exception e ) {
            // None
        }

        return loc;
    }

    private void releaseMemory() {
        dataMap = null;
        materialMap = null;
        sortedLocStrList = null;
    }
}
