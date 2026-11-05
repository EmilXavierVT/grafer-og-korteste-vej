package grafer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

record Route<T>(List<T> nodes, long distance) {
    static <T> Route<T> from(T goal, long distance, Map<T, T> predecessors) {
        Deque<T> path = new ArrayDeque<>();
        for (T node = goal; node != null; node = predecessors.get(node)) {
            path.addFirst(node);
        }
        return new Route<>(List.copyOf(path), distance);
    }
}
