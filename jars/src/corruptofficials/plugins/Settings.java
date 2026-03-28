package corruptofficials.plugins;

public enum Settings {

    CORRUPTION_CUTOFF("corruptofficials_cutoff", FieldType.INT, 5000);

    public enum FieldType { INT, DOUBLE, STRING, BOOLEAN }

    public final String id;
    public final FieldType type;
    public final Object defaultValue;

    Settings(String id, FieldType type, Object defaultValue) {
        this.id = id;
        this.type = type;
        this.defaultValue = defaultValue;
    }
}
