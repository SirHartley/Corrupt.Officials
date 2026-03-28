package corruptofficials.conditions;

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
        float income = market.getNetIncome();
        float maxIncomeBeforePenalty = SettingsListener.getInt(Settings.CORRUPTION_CUTOFF);
        float x = income / maxIncomeBeforePenalty;

        if (x < 1f) return;

        float incomeAboveCutoff = income - maxIncomeBeforePenalty;
        float reduction = (float) Math.pow(x, SettingsListener.getDouble(Settings.CORRUPTION_POW)); //% of the income above the limit that should not exist
        float penalty = incomeAboveCutoff - (incomeAboveCutoff * reduction); //credit value that the colony is earning over the target, should be deducted from income
        float red = (income - penalty) / income;

        //ModPlugin.log("income: " + income + " " + "maxIncomeBeforePenalty: " + maxIncomeBeforePenalty + " "+ "x: " + x + " "+ "incomeAboveCutoff: " + incomeAboveCutoff + " "+ "reduction: " + reduction + " " + "penalty: " + penalty + " " + " red factor " + red);

        //can't apply flat red to income directly, thx alex
        //market.getIndustry(Industries.POPULATION).getIncome().modifyFlat(getModId()+"_corruption", -penalty, "Corruption");
        market.getIncomeMult().modifyMult(getModId() + "_corruption", red, "Corruption");
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
