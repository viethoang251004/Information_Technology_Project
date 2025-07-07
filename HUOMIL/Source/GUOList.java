import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents a Utility Occupancy List (UO-list) used in the HUOPM algorithm.
 * Similar to UtilityList in HUI-Miner, but adapted for utility occupancy and remaining utility occupancy.
 */
public class GUOList {
    public int item;
    public List<Element> elements = new ArrayList<>();
    public double sumUO = 0;
    public double sumRUO = 0;

    public GUOList(int item) {
        this.item = item;
    }

    public void addElement(Element e) {
        elements.add(e);
        sumUO += e.uo;
        sumRUO += e.ruo;
    }

    public int size() {
        return elements.size();
    }

    public Element get(int index) {
        return elements.get(index);
    }

    public double computeUBUO(int minSup) {
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


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GUOList for item ").append(item).append("\n");
        sb.append("  sumUO: ").append(sumUO).append(", sumRUO: ").append(sumRUO).append("\n");
        sb.append("  Elements:\n");

        for (int i = 0; i < elements.size(); i++) {
            Element e = elements.get(i);
            sb.append("    [").append(i).append("] ");
            sb.append("uo=").append(String.format("%.4f", e.uo)).append(", ");
            sb.append("ruo=").append(String.format("%.4f", e.ruo)).append(", ");
            sb.append("nextItem=").append(e.nextItem).append(", ");
            sb.append("nextIndex=").append(e.nextIndex).append("\n");
        }

        return sb.toString();
    }
}
