package grafer;

import grafer.AStar.Cell;
import grafer.Dijkstra.Road;

import java.util.List;
import java.util.Map;

final class DemoMaps {
    static final List<List<String>> ROAD_LAYOUT = List.of(
            List.of("Skovby", "Lilleby", "Højby"),
            List.of("Engby", "Søby", "Fjordby"),
            List.of("Markby", "Broby", "Havneby"));

    static final Map<String, List<Road>> ROADS = Map.of(
            "Skovby", List.of(new Road("Lilleby", 12), new Road("Engby", 4)),
            "Lilleby", List.of(new Road("Højby", 6)),
            "Højby", List.of(new Road("Fjordby", 4)),
            "Engby", List.of(new Road("Søby", 3), new Road("Markby", 22)),
            "Søby", List.of(new Road("Lilleby", 2), new Road("Fjordby", 14), new Road("Broby", 8)),
            "Fjordby", List.of(new Road("Havneby", 4)),
            "Markby", List.of(new Road("Broby", 6)),
            "Broby", List.of(new Road("Havneby", 9)),
            "Havneby", List.of());

    static final List<String> GRID = List.of(
            "....#.....",
            "....#..#..",
            "..#.#..#..",
            "..#.#.....",
            "....####..",
            ".#........",
            ".####.##..",
            "..........",
            "...#..#...",
            "..........");

    // Indekserne er nulbaserede. Kortet viser række og kolonne fra 1.
    static final Map<String, Cell> GRID_CITIES = Map.of(
            "Nordby", new Cell(1, 1),
            "Østby", new Cell(1, 8),
            "Vestby", new Cell(7, 1),
            "Sydby", new Cell(8, 7));
}
