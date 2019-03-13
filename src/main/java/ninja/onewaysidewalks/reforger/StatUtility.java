package ninja.onewaysidewalks.reforger;

public class StatUtility {
    public static boolean containsPhysDefense(String line) {
        return line.contains("phys") && line.contains("defense");
    }

    public static boolean containsMagicDefense(String line) {
        return line.contains("magic") && line.contains("defense");
    }

    public static boolean containsSoulReap(String line) {
        return line.contains("soul") || line.contains("reap");
    }

    public static boolean containsSpritualAttack(String line) {
        return (line.contains("spirit") || line.contains("spirual"))
                && (line.contains("attack") || (line.contains("atack")));
    }

    public static boolean containsPhysicalCrit(String line) {
        return line.contains("p") && (line.contains("cri") || line.contains("it"));
    }

    public static boolean containsMagicalCrit(String line) {
        return line.contains("m") && (line.contains("cri") || line.contains("it"));
    }

    public static boolean containsDecendingInferno(String line) {
        return (line.contains("fern")
                || line.contains("dece")
                || line.contains("desce")
                || line.contains("sce")
                || line.contains("ifer")
                || line.contains("ding")
                || line.contains("fero")
                || line.contains("erno")
                || line.contains("ending")
        );
    }

    public static boolean containsIntellect(String line) {
        return line.contains("intellect")
                || line.contains("lect")
                || line.contains("intl")
                || line.contains("llec");
    }

    public static boolean containsMagicDamage(String line) {
        return (line.contains("magic")
                || line.contains("mgi")
                || line.contains("mag"))
                && (line.contains("dam")
                || line.contains("dma")
                || line.contains("age"));
    }

    public static boolean containsParry(String line) {
        return line.contains("parry")
                || line.contains("par")
                || line.contains("arr");
    }
}
