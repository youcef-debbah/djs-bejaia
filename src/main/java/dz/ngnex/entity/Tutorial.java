package dz.ngnex.entity;

@SuppressWarnings("PointlessBitwiseExpression")
public enum Tutorial {
    OPEN_DOSSIER_UPLOADER(1 << 0),
    ;

    private final int mask;

    Tutorial(int mask) {
        this.mask = mask;
    }

    public int getMask() {
        return mask;
    }

    public int addTo(int state) {
        return state | mask;
    }

    public int removeFrom(int state) {
        return state & ~mask;
    }

    public boolean isIn(int state) {
        return (state & mask) != 0;
    }
}
