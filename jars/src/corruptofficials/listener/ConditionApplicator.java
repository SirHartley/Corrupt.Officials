package corruptofficials.listener;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.campaign.listeners.PlayerColonizationListener;
import corruptofficials.conditions.CorruptOfficialsConditionPlugin;

public class ConditionApplicator implements PlayerColonizationListener, EconomyTickListener {

    public static void applyResourceConditionToAllMarkets() {
        for (MarketAPI m : Global.getSector().getEconomy().getMarketsCopy()) {
            CorruptOfficialsConditionPlugin.applyConditionToMarket(m);
        }
    }

    public static void register() {
        Global.getSector().getListenerManager().addListener(new ConditionApplicator(), true);
    }

    @Override
    public void reportPlayerColonizedPlanet(PlanetAPI planetAPI) {
        MarketAPI m = planetAPI.getMarket();
        CorruptOfficialsConditionPlugin.applyConditionToMarket(m);
    }

    @Override
    public void reportPlayerAbandonedColony(MarketAPI marketAPI) {

    }

    @Override
    public void reportEconomyTick(int i) {
        applyResourceConditionToAllMarkets();
    }

    @Override
    public void reportEconomyMonthEnd() {

    }
}
