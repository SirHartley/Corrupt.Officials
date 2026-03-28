package corruptofficials.conditions;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.PopulationAndInfrastructure;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import corruptofficials.plugins.ModPlugin;
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
        if (!SettingsListener.getBoolean(Settings.CORRUPTION_ENABLED)) return; //turned off? Leave

        Industry population = getPopulation();
        if (population == null) return;

        population.getIncome().unmodify(getModId());

        float income = market.getNetIncome();
        float maxIncomeBeforePenalty = SettingsListener.getInt(Settings.CORRUPTION_CUTOFF);
        float x = income / maxIncomeBeforePenalty;

        if (x < 1f) return; //income is below threshold

        float incomeAboveCutoff = income - maxIncomeBeforePenalty;
        float reduction = (float) Math.pow(x, SettingsListener.getDouble(Settings.CORRUPTION_POW)); //% of the income above the limit that should not exist
        float penalty = incomeAboveCutoff - (incomeAboveCutoff * reduction); //credit value that the colony is earning over the target, should be deducted from income
        float targetIncome = maxIncomeBeforePenalty + incomeAboveCutoff - penalty;
        float actualReductionAbs = income - targetIncome;

        float currentMarketIncomeMult = market.getIncomeMult().getModifiedValue(); //the reduction is affected by the overall market income mult so we "remove" it by division
        actualReductionAbs /= currentMarketIncomeMult;

        ModPlugin.log("net income: " + income
                + "\ngross income " + market.getGrossIncome()
                + "\nmaxIncomeBeforePenalty: " + maxIncomeBeforePenalty
                + "\nincomeAboveCutoff: " + incomeAboveCutoff
                + "\nnew penalty: " + penalty
                + "\ntarget income: " + targetIncome
                + "\npercent income above cutoff that should not exist: " + reduction);

        //flat industry income of industries gets reduced by total income mult so we can't use that for corruption or it'll always be in excess
        population.getIncome().modifyFlatAlways(getModId(), -actualReductionAbs, "Government Corruption");
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        market.getIncomeMult().unmodify(getModId());
        market.getIncomeMult().unmodify(getModId() + "_corruption");

        Industry population = getPopulation();
        if (population != null) market.getIndustry(Industries.POPULATION).getIncome().unmodify(getModId());
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

    public Industry getPopulation(){
        Industry population = market.getIndustry(Industries.POPULATION);

        if (population == null){
            for (Industry ind : market.getIndustries()) if (ind instanceof PopulationAndInfrastructure) {
                population = ind;
                break;
            }
        }

        return population;
    }
}
