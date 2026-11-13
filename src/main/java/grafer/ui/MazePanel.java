package grafer.ui;

import grafer.AStar;
import grafer.AStar.Cell;
import grafer.SearchProgress;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Map;

final class MazePanel extends SearchPanel {
    static final String DEFAULT_START = "Nordby";
    static final String DEFAULT_GOAL = "Østby";
    private static final List<String> GRID = List.of(
            "....#.....",
            "....#..#..",
            "..#.#..#..",
            "..#.#.....",
            "....####..",
            ".#........",
            ".####.##..",
            "..........",
            "...#..#...",
            ".........."
    );

    private static final Map<String, Cell> CITIES = Map.of(
            "Nordby", new Cell(1, 1),
            "Østby", new Cell(1, 8),
            "Vestby", new Cell(7, 1),
            "Sydby", new Cell(8, 7)
    );
    private static final int TILE = 41;
    private static final int LEFT = (MAP_WIDTH - TILE * GRID.get(0).length()) / 2;
    private static final int TOP = 39;
    private static final Color WALL = new Color(43, 57, 77);
    private static final Color FAINT_SCENT = new Color(255, 249, 234);
    private static final Color STRONG_SCENT = new Color(255, 193, 82);
    private static final Color SCENT_TEXT = new Color(106, 65, 24);

    private Cell start;
    private Cell goal;
    private AStar search;
    private SearchProgress<Cell> progress;
    private int mouseIndex;

    MazePanel() {
        super("Queue  ·  lowest f = g + h first");
        reset(DEFAULT_START, DEFAULT_GOAL);
    }

    @Override
    String[] cityNames() {
        return CITIES.keySet().stream().sorted().toArray(String[]::new);
    }

    @Override
    void reset(String startName, String goalName) {
        start = CITIES.get(startName);
        goal = CITIES.get(goalName);
        search = new AStar(GRID, start, goal);
        progress = search.progress();
        mouseIndex = 0;
        repaint();
    }

    @Override
    boolean finished() {
        List<Cell> route = progress.route();
        return route.isEmpty() ? progress.finished() : mouseIndex == route.size() - 1;
    }

    @Override
    void step() {
        if (finished()) {
            return;
        }
        if (progress.route().isEmpty()) {
            progress = search.step();
        } else {
            mouseIndex++;
        }
        repaint();
    }

    @Override
    String status() {
        if (!progress.route().isEmpty()) {
            int moves = progress.route().size() - 1;
            if (finished()) {
                return "Cheese reached · " + moves + " moves";
            }
            return "Shortest route · " + moves + " moves\nMouse has walked " + mouseIndex + " of " + moves + " moves.";
        }
        if (finished()) {
            return "The mouse has no route to the cheese.";
        }
        Cell current = progress.current();
        if (current == null) {
            return "Ready to find the cheese.\n"
                    + "Each tile shows its scent estimate h. Warmer tiles have stronger scent.";
        }
        long g = progress.distances().get(current);
        long h = search.estimate(current);
        return "Search step " + progress.visited().size() + " · tile " + coordinates(current) + "\n"
                + "g = " + g + "    h = " + h + "    f = " + (g + h)
                + "\ng counts moves. h estimates moves to the cheese.";
    }

    @Override
    String queueText() {
        if (!progress.route().isEmpty()) {
            return finished() ? "Mouse reached the cheese." : "Route found.\nFollow the green line.";
        }
        if (finished()) {
            return "Queue empty.";
        }
        StringBuilder text = new StringBuilder("  Tile       g   h   f\n\n");
        List<Cell> queue = progress.queue();
        for (int i = 0; i < queue.size(); i++) {
            Cell cell = queue.get(i);
            long g = progress.distances().get(cell);
            long h = search.estimate(cell);
            text.append(String.format("%s %-8s %3d %3d %3d%n",
                    i == 0 ? "→" : " ", coordinates(cell), g, h, g + h));
        }
        return text.toString();
    }

    private static String coordinates(Cell cell) {
        return "(" + (cell.row() + 1) + "," + (cell.column() + 1) + ")";
    }

    @Override
    void drawBoard(Graphics2D drawing) {
        drawing.setColor(WALL);
        drawing.fillRoundRect(LEFT - 6, TOP - 6, GRID.get(0).length() * TILE + 11, GRID.size() * TILE + 11, 15, 15);
        drawing.setFont(getFont().deriveFont(Font.BOLD, 11f));

        for (int row = 0; row < GRID.size(); row++) {
            drawing.setColor(MUTED);
            centerText(drawing, Integer.toString(row + 1), LEFT - 21, TOP + row * TILE + 24);
            for (int column = 0; column < GRID.get(row).length(); column++) {
                if (row == 0) {
                    drawing.setColor(MUTED);
                    centerText(drawing, Integer.toString(column + 1), LEFT + column * TILE + TILE / 2, TOP - 13);
                }
                drawTile(drawing, new Cell(row, column));
            }
        }

        List<Cell> route = progress.route();
        drawing.setColor(ROUTE);
        drawing.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 1; i < route.size(); i++) {
            Cell from = route.get(i - 1);
            Cell to = route.get(i);
            drawing.drawLine(centerX(from), centerY(from), centerX(to), centerY(to));
        }
        drawCityNames(drawing);

        Cell mouse = route.isEmpty() ? start : route.get(mouseIndex);
        boolean atCheese = mouse.equals(goal);
        drawCheese(drawing, centerX(goal) + (atCheese ? 8 : 0), centerY(goal) - (atCheese ? 5 : 0));
        drawMouse(drawing, centerX(mouse) - (atCheese ? 4 : 0), centerY(mouse) + (atCheese ? 2 : 0));
    }

    private void drawTile(Graphics2D drawing, Cell cell) {
        int x = LEFT + cell.column() * TILE;
        int y = TOP + cell.row() * TILE;
        if (!search.isOpen(cell)) {
            drawing.setColor(WALL.brighter());
            drawing.fillRect(x, y, TILE - 1, TILE - 1);
            return;
        }

        long h = search.estimate(cell);
        int maxH = GRID.size() + GRID.get(0).length() - 2;
        double scent = 1 - h / (double) maxH;
        drawing.setColor(mix(FAINT_SCENT, STRONG_SCENT, scent * scent));
        drawing.fillRect(x, y, TILE - 1, TILE - 1);
        drawing.setColor(SCENT_TEXT);
        drawing.drawString(Long.toString(h), x + 4, y + 12);

        if (cell.equals(progress.current()) && progress.route().isEmpty()) {
            drawing.setColor(CURRENT);
            drawing.setStroke(new BasicStroke(2f));
            drawing.drawRect(x + 1, y + 1, TILE - 4, TILE - 4);
        } else if (progress.queue().contains(cell) || progress.visited().contains(cell)) {
            drawing.setColor(progress.queue().contains(cell) ? QUEUED : VISITED);
            drawing.fillOval(x + TILE - 11, y + 5, 6, 6);
        }
    }

    private void drawCityNames(Graphics2D drawing) {
        drawing.setFont(getFont().deriveFont(Font.BOLD, 8f));
        for (Map.Entry<String, Cell> city : CITIES.entrySet()) {
            int x = centerX(city.getValue());
            int y = centerY(city.getValue()) + 16;
            drawing.setColor(FAINT_SCENT);
            drawing.fillRoundRect(x - 18, y - 7, 36, 10, 5, 5);
            drawing.setColor(SCENT_TEXT);
            centerText(drawing, city.getKey(), x, y);
        }
    }

    private static int centerX(Cell cell) {
        return LEFT + cell.column() * TILE + TILE / 2;
    }

    private static int centerY(Cell cell) {
        return TOP + cell.row() * TILE + TILE / 2;
    }

    private static void drawCheese(Graphics2D drawing, int x, int y) {
        drawing.setColor(new Color(194, 115, 25));
        drawing.fillPolygon(new int[]{x - 12, x + 1, x + 12, x + 12, x - 12},
                new int[]{y - 3, y - 11, y - 5, y + 8, y + 8}, 5);
        drawing.setColor(new Color(255, 214, 85));
        drawing.fillPolygon(new int[]{x - 11, x + 1, x + 11, x + 11, x - 11},
                new int[]{y - 2, y - 10, y - 4, y + 7, y + 7}, 5);
        drawing.setColor(new Color(211, 136, 37));
        drawing.fillOval(x - 8, y, 5, 4);
        drawing.fillOval(x + 3, y - 3, 5, 5);
        drawing.fillOval(x + 1, y + 4, 3, 3);
    }

    private static void drawMouse(Graphics2D drawing, int x, int y) {
        drawing.setColor(new Color(160, 98, 117));
        drawing.setStroke(new BasicStroke(1.6f));
        drawing.drawArc(x - 17, y + 1, 12, 8, 165, 180);
        drawing.setColor(new Color(101, 112, 135));
        drawing.fillOval(x - 12, y - 7, 22, 15);
        drawing.fillOval(x + 2, y - 9, 13, 13);
        drawing.fillOval(x + 2, y - 13, 8, 8);
        drawing.setColor(new Color(232, 169, 183));
        drawing.fillOval(x + 4, y - 11, 4, 4);
        drawing.fillOval(x + 12, y - 2, 4, 4);
        drawing.setColor(INK);
        drawing.fillOval(x + 10, y - 5, 2, 2);
    }
}
