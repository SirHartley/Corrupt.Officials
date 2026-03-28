package corruptofficials.plugins;

import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SettingsListener implements LunaSettingsListener {

    private static final String MOD_ID = "corruptofficials";
    private static final String CSV_PATH = "data/config/LunaSettings.csv";
    private static final Map<String, Object> cache = new HashMap<>();

    public static void loadSettings() {
        try {
            JSONArray csv = Global.getSettings().getMergedSpreadsheetDataForMod("fieldID", CSV_PATH, MOD_ID);
            for (int i = 0; i < csv.length(); i++) {
                JSONObject row = csv.getJSONObject(i);
                String fieldID = row.getString("fieldID");
                for (Settings s : Settings.values()) {
                    if (s.id.equals(fieldID)) {
                        cache.put(s.id, parseDefault(row.getString("defaultValue"), s.type));
                    }
                }
            }
        } catch (IOException | JSONException e) {
            Global.getLogger(SettingsListener.class).error("Failed to load LunaSettings.csv defaults", e);
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

    private static Object parseDefault(String raw, Settings.FieldType type) {
        switch (type) {
            case INT:     return Integer.parseInt(raw.trim());
            case DOUBLE:  return Double.parseDouble(raw.trim());
            case BOOLEAN: return Boolean.parseBoolean(raw.trim());
            default:      return raw;
        }
    }

    @Override
    public void settingsChanged(String modID) {
        if (MOD_ID.equals(modID)) loadSettings();
    }

    public static int getInt(Settings setting)         { return (Integer) cache.getOrDefault(setting.id, 0); }
    public static double getDouble(Settings setting)   { return (Double)  cache.getOrDefault(setting.id, 0.0); }
    public static String getString(Settings setting)   { return (String)  cache.getOrDefault(setting.id, ""); }
    public static boolean getBoolean(Settings setting) { return (Boolean) cache.getOrDefault(setting.id, false); }
}
