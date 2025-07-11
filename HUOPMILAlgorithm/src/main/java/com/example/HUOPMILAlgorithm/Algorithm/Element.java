package com.example.HUOPMILAlgorithm.Algorithm;
public class Element {
    /** Utility Occupancy (uo) */
    public final float uo;

    /** Remaining Utility Occupancy (ruo) */
    public final float ruo;

    /** Next item in the same transaction (or -1 if none) */
    public int nextItem;

    /** Index of the entry in the next item's GUO-IL list (or -1 if none) */
    public int nextIndex;

    /**
     * Constructor.
     * @param uo         Utility Occupancy value
     * @param ruo        Remaining Utility Occupancy value
     * @param nextItem   Next item in the revised transaction (-1 if none)
     * @param nextIndex  Index of the next entry in GUO-IL of nextItem (-1 if none)
     */
    public Element(float uo, float ruo, int nextItem, int nextIndex) {
        this.uo = uo;
        this.ruo = ruo;
        this.nextItem = nextItem;
        this.nextIndex = nextIndex;
    }

    @Override
    public String toString() {
        return String.format("<nextItem=%d, uo=%.4f, ruo=%.4f, nextIndex=%d>", nextItem, uo, ruo, nextIndex);
    }
}
