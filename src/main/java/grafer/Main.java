package grafer;

import grafer.AStar.Cell;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

public final class Main {
    private record Endpoints(String start, String goal) {}

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                runMenu();
            } else if (args.length == 1 && args[0].equals("--help")) {
                printUsage();
            } else if (args.length == 1 || args.length == 3) {
                List<String> cities = args.length == 3 ? List.of(args[1], args[2]) : List.of();
                run(args[0], cities, null);
            } else {
                throw new IllegalArgumentException("Angiv en algoritme og eventuelt både startby og målby.");
            }
        } catch (IllegalArgumentException error) {
            System.err.println(error.getMessage());
            printUsage();
            System.exit(2);
        }
    }

    private static void runMenu() {
        try (Scanner input = new Scanner(System.in, StandardCharsets.UTF_8)) {
            System.out.println("1  Dijkstra · vægtet, rettet vejnet");
            System.out.println("2  A* · grid med veje og forhindringer");
            System.out.print("Vælg 1 eller 2 > ");
            while (input.hasNextLine()) {
                String algorithm = switch (input.nextLine().strip()) {
                    case "1" -> "dijkstra";
                    case "2" -> "astar";
                    default -> null;
                };
                if (algorithm != null) {
                    run(algorithm, List.of(), input);
                    return;
                }
                System.out.print("Vælg 1 eller 2 > ");
            }
        }
    }

    private static void run(String algorithm, List<String> cities, Scanner input) {
        switch (algorithm) {
            case "dijkstra" -> {
                Endpoints endpoints = endpoints(DemoMaps.ROADS.keySet(), "Skovby", "Havneby", cities, input);
                ConsoleView<String> view = new ConsoleView<>(false, Function.identity(), ConsoleMaps::printRoads, input);
                view.begin(endpoints.start(), endpoints.goal());
                view.finish(Dijkstra.findPath(DemoMaps.ROADS, endpoints.start(), endpoints.goal(), view));
            }
            case "astar" -> {
                Endpoints endpoints = endpoints(DemoMaps.GRID_CITIES.keySet(), "Nordby", "Østby", cities, input);
                Cell start = DemoMaps.GRID_CITIES.get(endpoints.start());
                Cell goal = DemoMaps.GRID_CITIES.get(endpoints.goal());
                ConsoleView<Cell> view = new ConsoleView<>(true, ConsoleMaps::cellLabel, ConsoleMaps::printGrid, input);
                view.begin(start, goal);
                view.finish(AStar.findPath(DemoMaps.GRID, start, goal, view));
            }
            default -> throw new IllegalArgumentException("Ukendt algoritme " + algorithm + ".");
        }
    }

    private static Endpoints endpoints(Set<String> cities, String defaultStart, String defaultGoal,
                                       List<String> arguments, Scanner input) {
        System.out.println("Byer · " + String.join(", ", new TreeSet<>(cities)));
        String start = selectCity(cities, defaultStart, "Startby", arguments.isEmpty() ? "" : arguments.get(0), input);
        String goal = selectCity(cities, defaultGoal, "Målby", arguments.isEmpty() ? "" : arguments.get(1), input);
        return new Endpoints(start, goal);
    }

    private static String selectCity(Set<String> cities, String fallback, String prompt, String argument, Scanner input) {
        while (true) {
            String name = argument.strip();
            if (input != null) {
                System.out.printf("%s [%s] > ", prompt, fallback);
                name = input.hasNextLine() ? input.nextLine().strip() : "";
            }
            if (name.isEmpty()) {
                return fallback;
            }
            for (String city : cities) {
                if (city.equalsIgnoreCase(name)) {
                    return city;
                }
            }
            if (input == null) {
                throw new IllegalArgumentException("Ukendt by " + name + ".");
            }
            System.out.printf("Byen %s findes ikke på kortet.%n", name);
        }
    }

    private static void printUsage() {
        System.out.println("Kør uden argumenter for at åbne den trinvise menu.");
        System.out.println("java -cp target/classes grafer.Main dijkstra|astar [startby målby]");
    }
}
