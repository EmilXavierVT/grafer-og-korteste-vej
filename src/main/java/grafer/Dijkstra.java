package grafer;

import grafer.SearchStep.Candidate;
import grafer.SearchStep.Change;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Consumer;

final class Dijkstra {
    record Road(String to, int kilometers) {
        Road {
            if (kilometers < 0) {
                throw new IllegalArgumentException("Vejens vægt skal være mindst 0 km.");
            }
        }
    }

    static Optional<Route<String>> findPath(Map<String, List<Road>> roads, String start, String goal,
                                            Consumer<SearchStep<String>> onStep) {
        Map<String, Long> distances = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();
        Set<String> settled = new LinkedHashSet<>();
        PriorityQueue<Candidate<String>> queue = new PriorityQueue<>(
                Comparator.comparingLong(Candidate<String>::g).thenComparing(Candidate::node));

        distances.put(start, 0L);
        queue.add(new Candidate<>(start, 0, 0));

        while (!queue.isEmpty()) {
            Candidate<String> current = queue.remove();
            if (!settled.add(current.node())) {
                continue;
            }

            boolean reachedGoal = current.node().equals(goal);
            List<Change<String>> changes = new ArrayList<>();
            if (!reachedGoal) {
                for (Road road : roads.getOrDefault(current.node(), List.of())) {
                    long candidate = current.g() + road.kilometers();
                    Long oldDistance = distances.get(road.to());
                    if (oldDistance == null || candidate < oldDistance) {
                        distances.put(road.to(), candidate);
                        predecessors.put(road.to(), current.node());
                        queue.add(new Candidate<>(road.to(), candidate, 0));
                        changes.add(new Change<>(road.to(), oldDistance, candidate));
                    }
                }
            }

            onStep.accept(SearchStep.capture(current, queue, distances, settled, changes, reachedGoal));
            if (reachedGoal) {
                return Optional.of(Route.from(goal, current.g(), predecessors));
            }
        }
        return Optional.empty();
    }
}
