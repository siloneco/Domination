package net.azisaba.silo.domination;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

public class DominationConfig {

    private final Domination plugin;
    private final FileConfiguration conf;

    @ConfigOptions(path = "Game.MinPlayers")
    public int minPlayers = 2;
    @ConfigOptions(path = "Game.MaxPlayers")
    public int maxPlayers = 12;
    @ConfigOptions(path = "Game.StartCountdownSeconds")
    public int startCountdownSeconds = 15;
    @ConfigOptions(path = "Game.GameLongSeconds")
    public int gameLongSecond = 600;
    @ConfigOptions(path = "Game.GameEndPoint")
    public int gameEndPoint = 1500;
    @ConfigOptions(path = "Game.KilledStrengthSeconds")
    public double killedStrengthSeconds = 3;
    @ConfigOptions(path = "Game.StandByGame")
    public int standByGameCount = 5;
    @ConfigOptions(path = "Game.IdLength")
    public int gameIDLength = 4;
    @ConfigOptions(path = "Game.InvincibleSeconds")
    public double invincibleSeconds = 3d;
    @ConfigOptions(path = "Game.UseGoldenHead")
    public boolean useGoldenHead = true;

    @ConfigOptions(path = "Map.SavePath")
    public String mapDataSavePath = "./plugins/Domination/MapData/";
    @ConfigOptions(path = "Map.DeleteConfigOnImport")
    public boolean deleteImportConfig = true;
    @ConfigOptions(path = "Map.GeneratorStopMilliSeconds")
    public int generatorStopMilliSeconds = 50;
    @ConfigOptions(path = "Map.GeneratorTimerTicks")
    public int generatorTimerTicks = 5;

    @ConfigOptions(path = "Scoreboard.UpdateTicks")
    public int scoreboardUpdateTicks = 19;

    @ConfigOptions(path = "Flag.Radius")
    public double flagRadius = 3.0;
    @ConfigOptions(path = "Flag.AdjustBarLocation")
    public boolean adjustFlagBarLocation = false;

    @ConfigOptions(path = "Log.Enable")
    public boolean enableLogging = true;
    @ConfigOptions(path = "Log.Path")
    public String logFolderPath = "./plugins/Domination/GameLog/";

    public DominationConfig(Domination plugin) {
        this.plugin = plugin;
        conf = plugin.getConfig();
    }

    public void loadConfig() {
        for ( Field field : getClass().getFields() ) {
            ConfigOptions anno = field.getAnnotation(ConfigOptions.class);

            if ( anno == null ) {
                continue;
            }

            String path = anno.path();

            if ( conf.get(path) == null ) {

                try {

                    if ( anno.type() == OptionType.NONE ) {
                        conf.set(path, field.get(this));
                    } else if ( anno.type() == OptionType.LOCATION ) {
                        Location loc = (Location) field.get(this);

                        conf.set(path, loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ()
                                + "," + loc.getYaw() + "," + loc.getPitch());
                    } else if ( anno.type() == OptionType.CHAT_FORMAT ) {

                        String msg = (String) field.get(this);
                        conf.set(path, msg);

                        msg = msg.replace("&", "§");
                        field.set(this, msg);
                    } else if ( anno.type() == OptionType.SOUND ) {
                        conf.set(path, field.get(this).toString());
                    } else if ( anno.type() == OptionType.LOCATION_LIST ) {
                        @SuppressWarnings("unchecked")
                        List<Location> locations = (List<Location>) field.get(this);

                        List<String> strs = new ArrayList<>();

                        if ( !locations.isEmpty() ) {

                            for ( Location loc : locations ) {
                                strs.add(loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + ","
                                        + loc.getZ()
                                        + "," + loc.getYaw() + "," + loc.getPitch());
                            }
                        } else {
                            strs.add("WorldName,X,Y,Z,Yaw,Pitch");
                        }

                        conf.set(path, strs);
                    }

                    plugin.saveConfig();
                } catch ( Exception e ) {
                    Bukkit.getLogger().warning("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {

                try {
                    if ( anno.type() == OptionType.NONE ) {
                        field.set(this, conf.get(path));
                    } else if ( anno.type() == OptionType.LOCATION ) {

                        String[] strings = conf.getString(path).split(",");
                        Location loc = null;
                        try {
                            loc = new Location(Bukkit.getWorld(strings[0]), Double.parseDouble(strings[1]),
                                    Double.parseDouble(strings[2]), Double.parseDouble(strings[3]));
                            loc.setYaw(Float.parseFloat(strings[4]));
                            loc.setPitch(Float.parseFloat(strings[5]));
                        } catch ( Exception e ) {
                            // None
                        }

                        if ( loc == null ) {
                            Bukkit.getLogger().warning("Error. " + path + " の値がロードできませんでした。");
                            continue;
                        }

                        field.set(this, loc);
                    } else if ( anno.type() == OptionType.SOUND ) {

                        String name = conf.getString(path);
                        Sound sound;

                        try {
                            sound = Sound.valueOf(name.toUpperCase());
                        } catch ( Exception e ) {
                            Bukkit.getLogger().warning("Error. " + path + " の値がロードできませんでした。");
                            continue;
                        }

                        field.set(this, sound);
                    } else if ( anno.type() == OptionType.CHAT_FORMAT ) {

                        String unformatMessage = conf.getString(path);

                        unformatMessage = unformatMessage.replace("&", "§");

                        field.set(this, unformatMessage);
                    } else if ( anno.type() == OptionType.LOCATION_LIST ) {

                        List<String> strList = conf.getStringList(path);

                        List<Location> locList = new ArrayList<>();

                        for ( String str : strList ) {

                            String[] strings = str.split(",");
                            Location loc = null;
                            try {
                                loc = new Location(Bukkit.getWorld(strings[0]), Double.parseDouble(strings[1]),
                                        Double.parseDouble(strings[2]), Double.parseDouble(strings[3]));
                                loc.setYaw(Float.parseFloat(strings[4]));
                                loc.setPitch(Float.parseFloat(strings[5]));
                            } catch ( Exception e ) {
                                // None
                            }

                            if ( loc == null ) {
                                Bukkit.getLogger().warning("Error. " + path + " の " + str + "がロードできませんでした。");
                                continue;
                            }

                            locList.add(loc);
                        }

                        field.set(this, locList);
                    }
                } catch ( Exception e ) {
                    Bukkit.getLogger().warning("Error. " + e.getMessage());
                }
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ConfigOptions {
        public String path();

        public OptionType type() default OptionType.NONE;
    }

    public enum OptionType {
        LOCATION,
        LOCATION_LIST,
        SOUND,
        CHAT_FORMAT,
        NONE
    }
}
