package corruptofficials.plugins;

import com.fs.starfarer.api.BaseModPlugin;
import corruptofficials.listener.ConditionApplicator;


public class ModPlugin extends BaseModPlugin {

    @Override
    public void onGameLoad(boolean newGame) {
        super.onGameLoad(newGame);

        ConditionApplicator.register();
        ConditionApplicator.applyResourceConditionToAllMarkets();
    }
}
