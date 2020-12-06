package ninja.onewaysidewalks.reforger;

public class StatUtility {
    public static boolean containsPhysicalCrit(String line) {
        return line.contains("p") && (line.contains("cri") || (line.contains("it") && !line.contains("vit")))
                && !line.contains("magic") && !line.contains("spi") && !line.contains("agi");
    }
}
