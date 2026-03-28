package corruptofficials.plugins;

import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;

import java.util.HashMap;
import java.util.Map;

public class SettingsListener implements LunaSettingsListener {

    private static final String MOD_ID = "corruptofficials";
    private static final Map<String, Object> cache = new HashMap<>();

    public static void loadSettings() {
        for (Settings s : Settings.values()) {
            cache.put(s.id, s.defaultValue);
        }
        if (!Global.getSettings().getModManager().isModEnabled("lunalib")) return;
        for (Settings s : Settings.values()) {
            Object val = null;
            switch (s.type) {
                case INT:     val = LunaSettings.getInt(MOD_ID, s.id);     break;
                case DOUBLE:  val = LunaSettings.getDouble(MOD_ID, s.id);  break;
                case STRING:  val = LunaSettings.getString(MOD_ID, s.id);  break;
                case BOOLEAN: val = LunaSettings.getBoolean(MOD_ID, s.id); break;
            }
            if (val != null) cache.put(s.id, val);
        }
    }

    @Override
    public void settingsChanged(String modID) {
        if (MOD_ID.equals(modID)) loadSettings();
    }

    public static int getInt(String id)         { return (Integer) cache.getOrDefault(id, 0); }
    public static double getDouble(String id)   { return (Double)  cache.getOrDefault(id, 0.0); }
    public static String getString(String id)   { return (String)  cache.getOrDefault(id, ""); }
    public static boolean getBoolean(String id) { return (Boolean) cache.getOrDefault(id, false); }
}
