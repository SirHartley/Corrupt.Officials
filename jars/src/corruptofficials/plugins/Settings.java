package corruptofficials.plugins;

public enum Settings {

    CORRUPTION_CUTOFF("corruptofficials_cutoff", FieldType.INT),
    CORRUPTION_MULT("corruptofficials_pow", FieldType.DOUBLE); //  public static final float CORRUPTION_INCOME_RED_POW = -0.6f;

    public enum FieldType { INT, DOUBLE, STRING, BOOLEAN }

    public final String id;
    public final FieldType type;

    Settings(String id, FieldType type) {
        this.id = id;
        this.type = type;
    }
}
