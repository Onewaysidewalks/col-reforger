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

        //Always compared with AND
        private Map<StatType, Integer> statToDesiredCount = new HashMap<>();
    }

    enum StatConfigComparitor {
        AND, OR, AND_NOT
    }

    enum StatType {
        MAGIC_DAMAGE,
        REIGNING_SWORD,
        INTELLECT,
        TEMPEST_RAGE,
        PARRY
    }
}
