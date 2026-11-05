package grafer;

import grafer.SearchStep.Candidate;
import grafer.SearchStep.Change;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

final class ConsoleView<T> implements Consumer<SearchStep<T>> {
    private final boolean heuristic;
    private final Function<T, String> label;
    private final Consumer<Map<T, Character>> drawMap;
    private final Scanner input;
    private boolean pause;
    private int stepNumber;
    private Map<T, Character> markers;

    ConsoleView(boolean heuristic, Function<T, String> label,
                Consumer<Map<T, Character>> drawMap, Scanner input) {
        this.heuristic = heuristic;
        this.label = label;
        this.drawMap = drawMap;
        this.input = input;
        this.pause = input != null;
    }

    void begin(T start, T goal) {
        System.out.printf("%n%s fra %s til %s%n", heuristic ? "A*" : "Dijkstra", label.apply(start), label.apply(goal));
        if (heuristic) {
            System.out.println("Laveste f = g + h vælges. Ved lighed afgør h, række og kolonne rækkefølgen.");
            System.out.println("g er antal træk fra start, h er Manhattan-afstanden til målet.");
            System.out.println("# er spærret. Bogstavet ved en celle er byens forbogstav.");
        } else {
            System.out.println("Laveste g vælges. Ved lighed sorteres efter bynavn.");
            System.out.println("g er antal kilometer fra start. Pilene viser retning og vægt for hver vej.");
        }
        System.out.println(". uopdaget   + i kø   @ valgt nu   x fastlagt   * korteste rute");
        drawMap.accept(Map.of(start, '+'));
        waitForStep();
    }

    @Override
    public void accept(SearchStep<T> step) {
        Candidate<T> chosen = step.chosen();
        String priority = heuristic
                ? "g = %d, h = %d, f = %d".formatted(chosen.g(), chosen.h(), chosen.f())
                : "g = %d km".formatted(chosen.g());
        System.out.printf("%nTrin %d · Valgt %s · %s%n", ++stepNumber, label.apply(chosen.node()), priority);

        markers = new HashMap<>();
        for (Candidate<T> candidate : step.frontier()) {
            markers.put(candidate.node(), '+');
        }
        for (T node : step.settled()) {
            markers.put(node, 'x');
        }
        markers.put(chosen.node(), '@');
        drawMap.accept(markers);

        if (step.goalReached()) {
            System.out.println("Målet er taget ud af køen. Afstanden er fastlagt.");
            return;
        }

        for (Change<T> change : step.changes()) {
            String before = change.oldDistance() == null ? "∞" : change.oldDistance().toString();
            System.out.printf("%s får nyt g · %s → %d via %s%n",
                    label.apply(change.node()), before, change.distance(), label.apply(chosen.node()));
        }
        printQueue(step.frontier());
        if (!step.frontier().isEmpty()) {
            waitForStep();
        }
    }

    private void printQueue(List<Candidate<T>> frontier) {
        if (frontier.isEmpty()) {
            System.out.println("Køen er tom.");
            return;
        }
        System.out.println("Kø efter trinnet (→ viser næste valg)");
        System.out.printf("    %-18s %4s", heuristic ? "Celle" : "By", "g");
        if (heuristic) {
            System.out.printf(" %4s %4s", "h", "f");
        }
        System.out.println();
        for (int i = 0; i < frontier.size(); i++) {
            Candidate<T> candidate = frontier.get(i);
            System.out.printf("  %s %-18s %4d", i == 0 ? "→" : " ", label.apply(candidate.node()), candidate.g());
            if (heuristic) {
                System.out.printf(" %4d %4d", candidate.h(), candidate.f());
            }
            System.out.println();
        }
    }

    private void waitForStep() {
        if (pause) {
            System.out.print("Enter for næste trin, a for alle resterende > ");
            if (!input.hasNextLine() || input.nextLine().strip().equalsIgnoreCase("a")) {
                pause = false;
            }
        }
    }

    void finish(Optional<Route<T>> result) {
        if (result.isEmpty()) {
            System.out.println("\nDer findes ingen rute mellem de valgte byer.");
            return;
        }
        Route<T> route = result.get();
        System.out.printf("%nKorteste rute · %d %s%n", route.distance(), heuristic ? "træk" : "km");
        System.out.println(route.nodes().stream().map(label).collect(Collectors.joining(" → ")));

        for (T node : route.nodes()) {
            markers.put(node, '*');
        }
        drawMap.accept(markers);
    }
}
