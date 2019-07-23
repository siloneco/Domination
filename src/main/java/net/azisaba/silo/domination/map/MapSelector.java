package net.azisaba.silo.domination.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.azisaba.silo.domination.Domination;

public class MapSelector {

    private final Domination plugin;

    private List<MapData> mapList = new ArrayList<>();

    public MapSelector(Domination plugin) {
        this.plugin = plugin;
    }

    public void loadMaps() {

        plugin.getLogger().info("Loading " + plugin.mapLoader.countMap() + " maps...");

        List<MapData> mapDataList = plugin.mapLoader.loadAllMapData();

        if ( mapDataList != null ) {
            mapList.addAll(mapDataList);
        }
    }

    public void setMapList(List<MapData> mapList) {
        this.mapList = mapList;
    }

    public MapData chooseRandomMap() {
        if ( mapList.size() == 0 ) {
            return null;
        }

        Collections.shuffle(mapList);
        return mapList.get(0).clone();
    }

    public MapData getMap(String mapName) {
        if ( mapList.size() == 0 ) {
            return null;
        }

        for ( MapData data : mapList ) {
            if ( data.getMapName().equalsIgnoreCase(mapName) ) {
                return data.clone();
            }
        }

        return null;
    }

    public void addMap(MapData data) {

        MapData remove = null;

        for ( MapData d : mapList ) {
            if ( d.getMapName().equalsIgnoreCase(data.getMapName()) ) {
                remove = d;
                break;
            }
        }

        if ( remove != null ) {
            mapList.remove(remove);
        }

        mapList.add(data);
    }

    public List<MapData> getAllMapData() {
        return mapList;
    }
}
