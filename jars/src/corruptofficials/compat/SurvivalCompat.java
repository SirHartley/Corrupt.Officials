package corruptofficials.compat;

import com.fs.starfarer.api.Global;
import corruptofficials.plugins.Settings;
import corruptofficials.plugins.SettingsListener;

public class SurvivalCompat {

    private static final String VANILLA_THRESHOLD_KEY = "ext_corruptionThreshold";

    private static Float vanillaThreshold;

    public static boolean isCorruptionActive() {
        if (!SettingsListener.getBoolean(Settings.CORRUPTION_ENABLED)) return false;
        return !Global.getSector().isExtMode() || SettingsListener.getBoolean(Settings.CORRUPTION_REPLACE_SURVIVAL);
    }

    public static void updateVanillaCorruption() {
        if (vanillaThreshold == null) vanillaThreshold = Global.getSettings().getFloat(VANILLA_THRESHOLD_KEY);

        boolean replace = SettingsListener.getBoolean(Settings.CORRUPTION_ENABLED)
                && SettingsListener.getBoolean(Settings.CORRUPTION_REPLACE_SURVIVAL);

        //only read by CoreScript in survival mode, so no mode check needed here
        Global.getSettings().setFloat(VANILLA_THRESHOLD_KEY, replace ? Float.MAX_VALUE : vanillaThreshold);
    }
}
