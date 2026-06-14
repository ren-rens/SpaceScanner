package bg.sofia.uni.fmi.mjt.space.mission;

public enum MissionStatus {

    SUCCESS("Success"),
    FAILURE("Failure"),
    PARTIAL_FAILURE("Partial Failure"),
    PRELAUNCH_FAILURE("Prelaunch Failure");

    private final String value;

    MissionStatus(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }

    public static MissionStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        for (MissionStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }

        return null; // incorrect mission status given
    }

}