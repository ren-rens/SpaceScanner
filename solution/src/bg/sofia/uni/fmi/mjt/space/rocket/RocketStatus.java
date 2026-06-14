package bg.sofia.uni.fmi.mjt.space.rocket;

public enum RocketStatus {

    STATUS_RETIRED("StatusRetired"),
    STATUS_ACTIVE("StatusActive");

    private final String value;

    RocketStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static RocketStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        for (RocketStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }

        return null; // incorrect rocket status given
    }
}