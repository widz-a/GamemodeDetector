package withicality.gamemodedetector;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "gamemodedetector")
public class TheConfig implements ConfigData {

    @ConfigEntry.Category("mod")
    @ConfigEntry.Gui.TransitiveObject
    public ConfigMod mod = new ConfigMod();
    public static class ConfigMod {
        public boolean enabled = true;
    }
}