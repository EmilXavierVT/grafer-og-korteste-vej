package grafer;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

record SearchStep<T>(Candidate<T> chosen, List<Candidate<T>> frontier, List<T> settled,
                     List<Change<T>> changes, boolean goalReached) {
    record Candidate<T>(T node, long g, long h) {
        long f() {
            return g + h;
        }
    }

    record Change<T>(T node, Long oldDistance, long distance) {}

    static <T> SearchStep<T> capture(Candidate<T> chosen, PriorityQueue<Candidate<T>> queue,
                                     Map<T, Long> distances, Set<T> settled,
                                     List<Change<T>> changes, boolean goalReached) {
        // En forbedring lægger en ny post i køen. Vis kun den gældende post for hver node.
        List<Candidate<T>> frontier = queue.stream()
                .filter(entry -> !settled.contains(entry.node()) && entry.g() == distances.get(entry.node()))
                .sorted(queue.comparator())
                .toList();
        return new SearchStep<>(chosen, frontier, List.copyOf(settled), List.copyOf(changes), goalReached);
    }
}
