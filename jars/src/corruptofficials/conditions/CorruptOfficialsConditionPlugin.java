package corruptofficials.conditions;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import corruptofficials.plugins.Settings;
import corruptofficials.plugins.SettingsListener;

public class CorruptOfficialsConditionPlugin extends BaseMarketConditionPlugin {

    public static final String ID = "corruptofficials_cond";

    @Override
    public void apply(String id) {
        super.apply(id);
        applyCorruption();
    }

    public void applyCorruption() {
        market.getIncomeMult().unmodify(getModId() + "_corruption");

        float net = market.getNetIncome();
        float cutoff = SettingsListener.getInt(Settings.CORRUPTION_CUTOFF);

        if (net <= cutoff) return;

        float x = net / cutoff;
        float excess = net - cutoff;
        float reduction = (float) Math.pow(x, SettingsListener.getDouble(Settings.CORRUPTION_POW));
        float creditPenalty = excess - (excess * reduction);

        float totalUpkeep = 0f;
        for (Industry industry : market.getIndustries()) {
            totalUpkeep += industry.getUpkeep().getModifiedValue();
        }
        float gross = net + totalUpkeep;

        if (gross <= 0f) return;

        float mult = (gross - creditPenalty) / gross;
        market.getIncomeMult().modifyMult(getModId() + "_corruption", mult, "Corruption");
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        market.getIncomeMult().unmodify(getModId());
        market.getIncomeMult().unmodify(getModId() + "_corruption");
    }

    @Override
    public boolean showIcon() {
        return false;
    }

    public String getModId() {
        return condition.getId();
    }

    public static void applyConditionToMarket(MarketAPI m) {
        if (m.isInEconomy() && !m.hasCondition(ID)) m.addCondition(ID);
    }
}
