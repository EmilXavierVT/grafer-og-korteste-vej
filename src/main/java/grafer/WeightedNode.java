package grafer;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class WeightedNode {
    private final String name;
    private final Map<WeightedNode, Integer> neighbors = new LinkedHashMap<>();

    public WeightedNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Map<WeightedNode, Integer> getNeighbors() {
        return Collections.unmodifiableMap(neighbors);
    }

    public void addNeighbor(WeightedNode neighbor, int weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("Vejens vægt skal være mindst 0 km.");
        }
        neighbors.put(neighbor, weight);
    }
}
