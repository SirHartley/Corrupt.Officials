package corruptofficials.plugins;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import corruptofficials.listener.ConditionApplicator;
import lunalib.lunaSettings.LunaSettings;


public class ModPlugin extends BaseModPlugin {

    @Override
    public void onApplicationLoad() throws Exception {
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            LunaSettings.addSettingsListener(new SettingsListener());
        }
        SettingsListener.loadSettings();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        super.onGameLoad(newGame);

        ConditionApplicator.register();
        ConditionApplicator.applyResourceConditionToAllMarkets();
    }
}
