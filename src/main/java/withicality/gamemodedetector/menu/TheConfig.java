package withicality.gamemodedetector.menu;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "gamemodedetector")
public class TheConfig implements ConfigData {

    @ConfigEntry.Category("main")
    @ConfigEntry.Gui.TransitiveObject
    public GamemodeConfig gamemode = new GamemodeConfig();
    public static class GamemodeConfig {
        public boolean enabled = true;
        public boolean actionbar = false;
        public String message = "&b[%time%] &fPlayer &7%player% &fis in &7%gamemode%&f.";
    }
}