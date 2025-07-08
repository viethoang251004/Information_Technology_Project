public class Element {
    /** Transaction ID */
    public final int tid;

    /** Utility Occupancy (uo) */
    public final double uo;

    /** Remaining Utility Occupancy (ruo) */
    public final double ruo;

    /**
     * Constructor.
     * @param tid  Transaction ID
     * @param uo   Utility Occupancy value
     * @param ruo  Remaining Utility Occupancy value
     */
    public Element(int tid, double uo, double ruo) {
        this.tid = tid;
        this.uo = uo;
        this.ruo = ruo;
    }

    @Override
    public String toString() {
        return String.format("TID: %d, UO: %.4f, RUO: %.4f", tid, uo, ruo);
    }
}
