package bg.sofia.uni.fmi.mjt.space.loader.constants;

public enum MissionIndex {
    ID(0),
    COMPANY(1),
    LOCATION(2),
    DATE(3),
    DETAIL(4),
    ROCKET_STATUS(5),
    COST(6),
    MISSION_STATUS(7);

    private final int index;

    MissionIndex(int i) {
        this.index = i;
    }

    public int getIndex() {
        return index;
    }
}
