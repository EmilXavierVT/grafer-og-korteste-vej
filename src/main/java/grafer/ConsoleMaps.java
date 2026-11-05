package grafer;

import grafer.AStar.Cell;
import grafer.Dijkstra.Road;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class ConsoleMaps {
    private static final Map<Cell, String> GRID_NAMES = DemoMaps.GRID_CITIES.entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getValue, Map.Entry::getKey));

    static void printRoads(Map<String, Character> markers) {
        List<List<String>> layout = DemoMaps.ROAD_LAYOUT;
        for (int row = 0; row < layout.size(); row++) {
            List<String> cities = layout.get(row);
            for (int column = 0; column < cities.size(); column++) {
                String city = cities.get(column);
                if (column > 0) {
                    System.out.printf(" %-7s", roadLabel(cities.get(column - 1), city, "→", "←"));
                }
                System.out.printf("[%c %-7s]", markers.getOrDefault(city, '.'), city);
            }
            System.out.println();

            if (row + 1 < layout.size()) {
                for (int column = 0; column < cities.size(); column++) {
                    String below = layout.get(row + 1).get(column);
                    System.out.printf("     %-14s", roadLabel(cities.get(column), below, "↓", "↑"));
                }
                System.out.println();
            }
        }
    }

    private static String roadLabel(String from, String to, String forward, String backward) {
        for (Road road : DemoMaps.ROADS.get(from)) {
            if (road.to().equals(to)) {
                return forward + " " + road.kilometers();
            }
        }
        for (Road road : DemoMaps.ROADS.get(to)) {
            if (road.to().equals(from)) {
                return backward + " " + road.kilometers();
            }
        }
        return "";
    }

    static void printGrid(Map<Cell, Character> markers) {
        System.out.print("     ");
        for (int column = 1; column <= DemoMaps.GRID.get(0).length(); column++) {
            System.out.printf("%2d ", column);
        }
        System.out.println();

        for (int row = 0; row < DemoMaps.GRID.size(); row++) {
            System.out.printf("%2d   ", row + 1);
            String line = DemoMaps.GRID.get(row);
            for (int column = 0; column < line.length(); column++) {
                Cell cell = new Cell(row, column);
                char marker = markers.getOrDefault(cell, line.charAt(column));
                char cityInitial = GRID_NAMES.getOrDefault(cell, " ").charAt(0);
                System.out.printf("%c%c ", marker, cityInitial);
            }
            System.out.println();
        }
    }

    static String cellLabel(Cell cell) {
        String position = "(%d,%d)".formatted(cell.row() + 1, cell.column() + 1);
        String city = GRID_NAMES.get(cell);
        return city == null ? position : city + " " + position;
    }
}
