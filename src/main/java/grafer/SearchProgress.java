package grafer;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A read-only snapshot of a search. The queue lists the next candidate first.
 * The current node is null before the first step, and the route is empty until the goal is reached.
 */
public record SearchProgress<T>(
        T current,
        List<T> queue,
        Set<T> visited,
        Map<T, Long> distances,
        List<T> route
) {
    public SearchProgress {
        queue = List.copyOf(queue);
        visited = Set.copyOf(visited);
        distances = Map.copyOf(distances);
        route = List.copyOf(route);
    }

    public boolean finished() {
        return !route.isEmpty() || queue.isEmpty();
    }
}
