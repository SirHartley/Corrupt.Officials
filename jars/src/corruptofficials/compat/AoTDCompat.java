package corruptofficials.compat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class AoTDCompat {

    private static final String AOTD_MOD_ID = "aotd_theory_of_toolbox";
    private static final String MISC_CLASS = "data.kaysaar.aotd.tot.misc.AoTDToolboxMisc";

    private static Boolean enabled;

    private static final MethodHandle setAccessibleHandle;
    private static final MethodHandle invokeHandle;
    private static final MethodHandle getNameHandle;
    private static final MethodHandle getParameterTypesHandle;

    static {
        try {
            Class<?> methodClass = Class.forName("java.lang.reflect.Method", false, Class.class.getClassLoader());
            MethodHandles.Lookup lookup = MethodHandles.lookup();

            setAccessibleHandle = lookup.findVirtual(methodClass, "setAccessible",
                    MethodType.methodType(void.class, boolean.class));
            invokeHandle = lookup.findVirtual(methodClass, "invoke",
                    MethodType.methodType(Object.class, Object.class, Object[].class));
            getNameHandle = lookup.findVirtual(methodClass, "getName",
                    MethodType.methodType(String.class));
            getParameterTypesHandle = lookup.findVirtual(methodClass, "getParameterTypes",
                    MethodType.methodType(Class[].class));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MethodHandle reflection", e);
        }
    }

    private static Object targetMethod;

    public static boolean isEnabled() {
        if (enabled == null) {
            enabled = Global.getSettings().getModManager().isModEnabled(AOTD_MOD_ID);
            if (enabled) {
                try {
                    Class<?> miscClass = Class.forName(MISC_CLASS);
                    targetMethod = findMethod(miscClass, "getExpectedMonthlyNetIncomeFromMarket",
                            new Class<?>[]{MarketAPI.class});
                    if (targetMethod == null) {
                        Global.getLogger(AoTDCompat.class).warn(
                                "AoTD detected but getExpectedMonthlyNetIncomeFromMarket not found");
                        enabled = false;
                    }
                } catch (Throwable e) {
                    Global.getLogger(AoTDCompat.class).warn("AoTD detected but class loading failed", e);
                    enabled = false;
                }
            }
        }
        return enabled;
    }

    public static float getNetIncome(MarketAPI market) {
        try {
            setAccessibleHandle.invoke(targetMethod, true);
            Object result = invokeHandle.invoke(targetMethod, null, new Object[]{market});
            return ((Number) result).floatValue();
        } catch (Throwable e) {
            Global.getLogger(AoTDCompat.class).warn("AoTD income call failed, falling back to vanilla", e);
            return market.getNetIncome();
        }
    }

    private static Object findMethod(Class<?> clazz, String name, Class<?>[] expectedParams) throws Throwable {
        for (Object method : clazz.getMethods()) {
            String methodName = (String) getNameHandle.invoke(method);
            if (!name.equals(methodName)) continue;

            Class<?>[] params = (Class<?>[]) getParameterTypesHandle.invoke(method);
            if (params.length != expectedParams.length) continue;

            boolean match = true;
            for (int i = 0; i < params.length; i++) {
                if (!params[i].isAssignableFrom(expectedParams[i])) {
                    match = false;
                    break;
                }
            }
            if (match) return method;
        }
        return null;
    }

    public static void reset() {
        enabled = null;
        targetMethod = null;
    }
}
