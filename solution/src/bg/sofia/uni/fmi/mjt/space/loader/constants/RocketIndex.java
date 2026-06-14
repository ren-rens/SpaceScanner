package bg.sofia.uni.fmi.mjt.space.loader.constants;

public enum RocketIndex {
    ID(0),
    NAME(1),
    WIKI(2),
    HEIGHT(3);

    private final int index;

    RocketIndex(int i) {
        this.index = i;
    }

    public int getIndex() {
        return index;
    }
}
