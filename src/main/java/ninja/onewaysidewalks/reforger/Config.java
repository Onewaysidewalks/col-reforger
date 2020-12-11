package ninja.onewaysidewalks.reforger;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Config {
    private List<String> windowNames;
    private String preprocessFile;

    private ExecutionConfig execution = new ExecutionConfig();

    private Map<StatType, StatConfig[]> stats = new HashMap<>();

    private boolean test;

    private String testFile;

    @Data
    @ToString
    static class StatConfig {
        private StatConfigComparitor type;
        private List<String> values = new ArrayList<>(); //always evaluated with OR
    }

    @Data
    @ToString
    static class ExecutionConfig {
        private int maxAttempts = 1;

        private float heightRatioForClick = .9f;
        private float widthRatioForClick = .92f;

        //Always compare list items with OR, but map items with AND
        private List<Map<StatType, Integer>> statToDesiredCount = new ArrayList<>();
    }

    enum StatConfigComparitor {
        AND, OR, AND_NOT
    }

    enum StatType {
        TEMPEST_RAGE,
        SEARING_CHAINS,
        MAGMA_SPIKES,
        ANVIL_DROP,
        METEOR_CRATER,
        DEADLY_FORGE,
        LAVA_QUAKE,
        FLAMING_SIGIL,
        MAGIC_DAMAGE,
        REIGNING_SWORD,
        INTELLECT,
        PARRY,
        DESCENDING_INFERNO,
        PHYSICAL_CRIT,
        MAGICAL_CRIT,
        SPIRITUAL_ATTACK,
        WEAKNESS_STRIKE,
        FLYING_BLADE,
        SHADOW_BLADE,
        ASSAULT,
        LACERATION,
        PIERCE,
        DASH,
        BLADE_TEMPEST
    }
}
