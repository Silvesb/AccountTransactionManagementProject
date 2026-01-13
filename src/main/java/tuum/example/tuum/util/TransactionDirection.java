package tuum.example.tuum.util;

public enum TransactionDirection {
    IN,
    OUT;

    public static TransactionDirection fromString(String value) {
        if (value == null) {
            throw new RuntimeException("Invalid direction");
        }
        try {
            return TransactionDirection.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid direction");
        }
    }
}
