package grafer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Searches a grid where '.' is open and each horizontal or vertical move costs one. */
public final class AStar {
    /** Zero-based grid coordinates. */
    public record Cell(int row, int column) {}

    private final List<String> grid;
    private final Cell goal;
    private final Map<Cell, Long> distances = new HashMap<>();
    private final Map<Cell, Cell> previous = new HashMap<>();
    private final Set<Cell> visited = new HashSet<>();
    private final List<Cell> queue = new ArrayList<>();
    private final List<Cell> route = new ArrayList<>();
    private Cell current;

    public AStar(List<String> grid, Cell start, Cell goal) {
        this.grid = List.copyOf(grid);
        if (!isOpen(start) || !isOpen(goal)) {
            throw new IllegalArgumentException("Start and goal must be open cells.");
        }
        this.goal = goal;
        distances.put(start, 0L);
        queue.add(start);
    }

    public SearchProgress<Cell> progress() {
        return new SearchProgress<>(current, queue, visited, distances, route);
    }

    public SearchProgress<Cell> step() {
        if (queue.isEmpty() || !route.isEmpty()) {
            return progress();
        }

        current = queue.remove(0);
        visited.add(current);
        if (current.equals(goal)) {
            for (Cell cell = goal; cell != null; cell = previous.get(cell)) {
                route.add(cell);
            }
            Collections.reverse(route);
            return progress();
        }

        List<Cell> neighbors = List.of(
                new Cell(current.row() - 1, current.column()),
                new Cell(current.row(), current.column() + 1),
                new Cell(current.row() + 1, current.column()),
                new Cell(current.row(), current.column() - 1)
        );
        long newDistance = distances.get(current) + 1;
        for (Cell next : neighbors) {
            if (!isOpen(next) || visited.contains(next)) {
                continue;
            }
            Long oldDistance = distances.get(next);
            if (oldDistance == null || newDistance < oldDistance) {
                distances.put(next, newDistance);
                previous.put(next, current);
                if (oldDistance == null) {
                    queue.add(next);
                }
            }
        }
        queue.sort(Comparator.comparingLong((Cell cell) -> distances.get(cell) + estimate(cell))
                .thenComparingLong(this::estimate)
                .thenComparingInt(Cell::row)
                .thenComparingInt(Cell::column));
        return progress();
    }

    public boolean isOpen(Cell cell) {
        return cell != null && cell.row() >= 0 && cell.row() < grid.size()
                && cell.column() >= 0 && cell.column() < grid.get(cell.row()).length()
                && grid.get(cell.row()).charAt(cell.column()) == '.';
    }

    public long estimate(Cell cell) {
        return Math.abs((long) cell.row() - goal.row()) + Math.abs((long) cell.column() - goal.column());
    }
}
