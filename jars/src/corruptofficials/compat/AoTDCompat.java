package corruptofficials.compat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

import java.lang.reflect.Method;

public class AoTDCompat {

    private static final String AOTD_MOD_ID = "aotd_theory_of_toolbox";
    private static final String MISC_CLASS = "data.kaysaar.aotd.tot.misc.AoTDToolboxMisc";

    private static Boolean enabled;
    private static Method getExpectedMonthlyNetIncomeFromMarket;

    public static boolean isEnabled() {
        if (enabled == null) {
            enabled = Global.getSettings().getModManager().isModEnabled(AOTD_MOD_ID);
            if (enabled) {
                try {
                    Class<?> miscClass = Class.forName(MISC_CLASS);
                    getExpectedMonthlyNetIncomeFromMarket = miscClass.getMethod(
                            "getExpectedMonthlyNetIncomeFromMarket", MarketAPI.class);
                } catch (Exception e) {
                    Global.getLogger(AoTDCompat.class).warn("AoTD detected but reflection failed", e);
                    enabled = false;
                }
            }
        }
        return enabled;
    }

    public static float getNetIncome(MarketAPI market) {
        try {
            Object result = getExpectedMonthlyNetIncomeFromMarket.invoke(null, market);
            return ((Number) result).floatValue();
        } catch (Exception e) {
            Global.getLogger(AoTDCompat.class).warn("AoTD income call failed, falling back to vanilla", e);
            return market.getNetIncome();
        }
    }

    public static void reset() {
        enabled = null;
        getExpectedMonthlyNetIncomeFromMarket = null;
    }
}
