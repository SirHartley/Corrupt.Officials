package corruptofficials.plugins;

public enum Settings {

    CORRUPTION_CUTOFF("corruptofficials_cutoff", FieldType.INT),
    CORRUPTION_ENABLED("corrputofficials_enabled", FieldType.BOOLEAN),
    CORRUPTION_POW("corruptofficials_pow", FieldType.DOUBLE);

    public enum FieldType { INT, DOUBLE, STRING, BOOLEAN }

    public final String id;
    public final FieldType type;

    Settings(String id, FieldType type) {
        this.id = id;
        this.type = type;
    }
}