import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents a Utility Occupancy List (UO-list) used in the HUOPM algorithm.
 * Similar to UtilityList in HUI-Miner, but adapted for utility occupancy and remaining utility occupancy.
 */
public class UOList {
    public Integer item;  // the item this list is associated with
    public double sumUO = 0.0;   // sum of all utility occupancy values
    public double sumRUO = 0.0;  // sum of all remaining utility occupancy values
    public List<Element> elements = new ArrayList<>();  // the list of (tid, uo, ruo)

    /**
     * Constructor
     * @param item the item that this UO-list is built for
     */
    public UOList(Integer item) {
        this.item = item;
    }

    /**
     * Add an Element to this list and update the totals
     * @param element the Element to add
     */
    public void addElement(Element element) {
        sumUO += element.uo;
        sumRUO += element.ruo;
        elements.add(element);
    }

    /**
     * @return number of transactions (support)
     */
    public int getSupport() {
        return elements.size();
    }

    /**
     * @return average utility occupancy (uo)
     */
    public double getAverageUO() {
        return elements.isEmpty() ? 0.0 : sumUO / elements.size();
    }

    /**
     * @return average remaining utility occupancy (ruo)
     */
    public double getAverageRUO() {
        return elements.isEmpty() ? 0.0 : sumRUO / elements.size();
    }

    /**
     * @return upper bound = average(uo + ruo)
     */
    public double getUpperBound(int minSup) {
            // Tạo vector chứa (uo + ruo) cho mỗi transaction
            List<Double> vec = new ArrayList<>();
            for (Element e : this.elements) {
                vec.add(e.uo + e.ruo);
            }

            // Sắp xếp giảm dần
            vec.sort(Collections.reverseOrder());
            // Lấy top-minSup phần tử
            int count = Math.min(minSup, vec.size());
            double sumTop = 0;
            for (int i = 0; i < count; i++) {
                sumTop += vec.get(i);
            }
            return sumTop / minSup;
    }

    /**
     * Print UO-list and summary
     */
    public void print() {
        
        for (Element e : elements) {
            System.out.printf("TID: %d, UO: %.4f, RUO: %.4f\n", e.tid, e.uo, e.ruo);
        }
        System.out.println("=== UO-list for item: " + item + " ===");
        System.out.printf("Support: %d\n", getSupport());
        System.out.printf("Avg UO : %.4f\n", getAverageUO());
        System.out.printf("Avg RUO: %.4f\n", getAverageRUO());
        System.out.println();
    }
}
