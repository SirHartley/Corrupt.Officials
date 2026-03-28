package corruptofficials.plugins;

public enum Settings {

    CORRUPTION_CUTOFF("corruptofficials_cutoff", FieldType.INT);

    public enum FieldType { INT, DOUBLE, STRING, BOOLEAN }

    public final String id;
    public final FieldType type;

    Settings(String id, FieldType type) {
        this.id = id;
        this.type = type;
    }
}
