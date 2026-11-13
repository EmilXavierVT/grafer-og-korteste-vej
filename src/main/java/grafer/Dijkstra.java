package grafer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Finds a shortest route through a fixed graph with nonnegative road weights. */
public final class Dijkstra {
    private final WeightedNode goal;
    private final Map<WeightedNode, Long> distances = new HashMap<>();
    private final Map<WeightedNode, WeightedNode> previous = new HashMap<>();
    private final Set<WeightedNode> visited = new HashSet<>();
    private final List<WeightedNode> queue = new ArrayList<>();
    private final List<WeightedNode> route = new ArrayList<>();
    private WeightedNode current;

    public Dijkstra(WeightedNode start, WeightedNode goal) {
        this.goal = Objects.requireNonNull(goal, "goal");
        distances.put(Objects.requireNonNull(start, "start"), 0L);
        queue.add(start);
    }

    public SearchProgress<WeightedNode> progress() {
        return new SearchProgress<>(current, queue, visited, distances, route);
    }

    public SearchProgress<WeightedNode> step() {
        if (queue.isEmpty() || !route.isEmpty()) {
            return progress();
        }

        current = queue.remove(0);
        visited.add(current);
        if (current == goal) {
            for (WeightedNode city = goal; city != null; city = previous.get(city)) {
                route.add(city);
            }
            Collections.reverse(route);
            return progress();
        }

        long distanceFromStart = distances.get(current);
        for (Map.Entry<WeightedNode, Integer> road : current.getNeighbors().entrySet()) {
            WeightedNode next = road.getKey();
            if (visited.contains(next)) {
                continue;
            }
            long newDistance = distanceFromStart + road.getValue();
            Long oldDistance = distances.get(next);
            if (oldDistance == null || newDistance < oldDistance) {
                distances.put(next, newDistance);
                previous.put(next, current);
                if (oldDistance == null) {
                    queue.add(next);
                }
            }
        }
        queue.sort(Comparator.comparingLong((WeightedNode city) -> distances.get(city))
                .thenComparing(WeightedNode::getName));
        return progress();
    }
}
