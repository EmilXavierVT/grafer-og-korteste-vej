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

final class AStar {
    record Cell(int row, int column) {
        List<Cell> neighbors() {
            return List.of(new Cell(row - 1, column), new Cell(row, column + 1),
                    new Cell(row + 1, column), new Cell(row, column - 1));
        }
    }

    static Optional<Route<Cell>> findPath(List<String> grid, Cell start, Cell goal,
                                          Consumer<SearchStep<Cell>> onStep) {
        if (!isRoad(grid, start) || !isRoad(grid, goal)) {
            throw new IllegalArgumentException("Start og mål skal ligge på vejceller i gridet.");
        }

        Map<Cell, Long> distances = new HashMap<>();
        Map<Cell, Cell> predecessors = new HashMap<>();
        Set<Cell> settled = new LinkedHashSet<>();
        PriorityQueue<Candidate<Cell>> queue = new PriorityQueue<>(
                Comparator.comparingLong(Candidate<Cell>::f)
                        .thenComparingLong(Candidate::h)
                        .thenComparingInt(entry -> entry.node().row())
                        .thenComparingInt(entry -> entry.node().column()));

        distances.put(start, 0L);
        queue.add(new Candidate<>(start, 0, manhattan(start, goal)));

        while (!queue.isEmpty()) {
            Candidate<Cell> current = queue.remove();
            if (!settled.add(current.node())) {
                continue;
            }

            boolean reachedGoal = current.node().equals(goal);
            List<Change<Cell>> changes = new ArrayList<>();
            if (!reachedGoal) {
                for (Cell next : current.node().neighbors()) {
                    if (!isRoad(grid, next)) {
                        continue;
                    }

                    long candidate = current.g() + 1;
                    Long oldDistance = distances.get(next);
                    if (oldDistance == null || candidate < oldDistance) {
                        distances.put(next, candidate);
                        predecessors.put(next, current.node());
                        queue.add(new Candidate<>(next, candidate, manhattan(next, goal)));
                        changes.add(new Change<>(next, oldDistance, candidate));
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

    private static boolean isRoad(List<String> grid, Cell cell) {
        return cell.row() >= 0 && cell.row() < grid.size()
                && cell.column() >= 0 && cell.column() < grid.get(cell.row()).length()
                && grid.get(cell.row()).charAt(cell.column()) == '.';
    }

    // Ét lovligt træk koster 1 og kan højst mindske Manhattan-afstanden med 1.
    private static long manhattan(Cell from, Cell goal) {
        return Math.abs((long) from.row() - goal.row()) + Math.abs((long) from.column() - goal.column());
    }
}
