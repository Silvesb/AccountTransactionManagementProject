package tuum.example.tuum.util;

public enum CurrencyType {
    EUR,
    SEK,
    GBP,
    USD;

    public static CurrencyType fromString(String value) {
        if (value == null) {
            throw new RuntimeException("Invalid currency");
        }
        try {
            return CurrencyType.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid currency");
        }
    }
}
