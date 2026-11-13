package grafer.ui;

import grafer.Dijkstra;
import grafer.SearchProgress;
import grafer.WeightedNode;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

final class RoadMapPanel extends SearchPanel {
    static final String DEFAULT_START = "Skovby";
    static final String DEFAULT_GOAL = "Havneby";
    private static final int CITY_WIDTH = 116;
    private static final int CITY_HEIGHT = 68;

    private final Map<String, WeightedNode> cities = new LinkedHashMap<>();
    private final Map<WeightedNode, Point> positions = new LinkedHashMap<>();
    private WeightedNode start;
    private WeightedNode goal;
    private Dijkstra search;
    private SearchProgress<WeightedNode> progress;

    RoadMapPanel() {
        super("Queue  ·  lowest g first");
        WeightedNode skovby = new WeightedNode("Skovby");
        WeightedNode lilleby = new WeightedNode("Lilleby");
        WeightedNode hojby = new WeightedNode("Højby");
        WeightedNode engby = new WeightedNode("Engby");
        WeightedNode soby = new WeightedNode("Søby");
        WeightedNode fjordby = new WeightedNode("Fjordby");
        WeightedNode markby = new WeightedNode("Markby");
        WeightedNode broby = new WeightedNode("Broby");
        WeightedNode havneby = new WeightedNode("Havneby");

        skovby.addNeighbor(lilleby, 12);
        skovby.addNeighbor(engby, 4);
        lilleby.addNeighbor(hojby, 6);
        hojby.addNeighbor(fjordby, 4);
        engby.addNeighbor(soby, 3);
        engby.addNeighbor(markby, 22);
        soby.addNeighbor(lilleby, 2);
        soby.addNeighbor(fjordby, 14);
        soby.addNeighbor(broby, 8);
        fjordby.addNeighbor(havneby, 4);
        markby.addNeighbor(broby, 6);
        broby.addNeighbor(havneby, 9);

        WeightedNode[][] map = {
                {skovby, lilleby, hojby},
                {engby, soby, fjordby},
                {markby, broby, havneby}
        };
        for (int row = 0; row < map.length; row++) {
            for (int column = 0; column < map[row].length; column++) {
                WeightedNode city = map[row][column];
                cities.put(city.getName(), city);
                positions.put(city, new Point(104 + 196 * column, 96 + 148 * row));
            }
        }
        reset(DEFAULT_START, DEFAULT_GOAL);
    }

    @Override
    String[] cityNames() {
        return cities.keySet().toArray(new String[0]);
    }

    @Override
    void reset(String startName, String goalName) {
        start = cities.get(startName);
        goal = cities.get(goalName);
        search = new Dijkstra(start, goal);
        progress = search.progress();
        repaint();
    }

    @Override
    boolean finished() {
        return progress.finished();
    }

    @Override
    void step() {
        progress = search.step();
        repaint();
    }

    @Override
    String status() {
        if (!progress.route().isEmpty()) {
            StringJoiner names = new StringJoiner(" → ");
            for (WeightedNode city : progress.route()) {
                names.add(city.getName());
            }
            return "Shortest route · " + progress.distances().get(goal) + " km\n" + names;
        }
        if (finished()) {
            return "No route from " + start.getName() + " to " + goal.getName() + ".";
        }
        WeightedNode current = progress.current();
        if (current == null) {
            return "Ready to search from " + start.getName() + " to " + goal.getName() + ".\n"
                    + "g is the known distance from the start.";
        }
        return "Step " + progress.visited().size() + " · " + current.getName()
                + "\ng = " + progress.distances().get(current) + " km from " + start.getName();
    }

    @Override
    String queueText() {
        if (finished()) {
            return progress.route().isEmpty() ? "Queue empty." : "Goal reached.";
        }
        StringBuilder text = new StringBuilder("  Town          g\n\n");
        List<WeightedNode> queue = progress.queue();
        for (int i = 0; i < queue.size(); i++) {
            WeightedNode city = queue.get(i);
            text.append(String.format("%s %-10s %4d km%n",
                    i == 0 ? "→" : " ", city.getName(), progress.distances().get(city)));
        }
        return text.toString();
    }

    @Override
    void drawBoard(Graphics2D drawing) {
        drawing.setFont(getFont().deriveFont(Font.BOLD, 12f));
        drawing.setColor(MUTED);
        drawing.drawString("ONE-WAY ROADS", 35, 35);
        drawing.setFont(getFont().deriveFont(12f));
        drawing.drawString("Road weights in km", 448, 35);

        for (WeightedNode from : positions.keySet()) {
            for (Map.Entry<WeightedNode, Integer> road : from.getNeighbors().entrySet()) {
                drawRoad(drawing, from, road.getKey(), road.getValue());
            }
        }
        for (WeightedNode city : positions.keySet()) {
            drawCity(drawing, city);
        }
    }

    private void drawRoad(Graphics2D drawing, WeightedNode from, WeightedNode to, int weight) {
        Point first = positions.get(from);
        Point last = positions.get(to);
        int dx = Integer.signum(last.x - first.x);
        int dy = Integer.signum(last.y - first.y);
        int x1 = first.x + dx * (CITY_WIDTH / 2 + 3);
        int y1 = first.y + dy * (CITY_HEIGHT / 2 + 3);
        int x2 = last.x - dx * (CITY_WIDTH / 2 + 6);
        int y2 = last.y - dy * (CITY_HEIGHT / 2 + 6);

        List<WeightedNode> route = progress.route();
        int fromIndex = route.indexOf(from);
        boolean onRoute = fromIndex >= 0 && fromIndex + 1 < route.size() && route.get(fromIndex + 1) == to;
        drawing.setColor(onRoute ? ROUTE : new Color(139, 158, 182));
        drawing.setStroke(new BasicStroke(onRoute ? 3.5f : 2f));
        drawing.drawLine(x1, y1, x2, y2);
        drawing.fillPolygon(
                new int[]{x2, x2 - 9 * dx + 5 * dy, x2 - 9 * dx - 5 * dy},
                new int[]{y2, y2 - 9 * dy - 5 * dx, y2 - 9 * dy + 5 * dx}, 3);

        int labelX = (x1 + x2) / 2 + (dx == 0 ? 21 : 0);
        int labelY = (y1 + y2) / 2 - (dy == 0 ? 14 : 0);
        drawing.setColor(onRoute ? new Color(222, 244, 235) : Color.WHITE);
        drawing.fillRoundRect(labelX - 16, labelY - 9, 32, 18, 9, 9);
        drawing.setFont(getFont().deriveFont(Font.BOLD, 12f));
        drawing.setColor(onRoute ? ROUTE : MUTED);
        centerText(drawing, Integer.toString(weight), labelX, labelY + 4);
    }

    private void drawCity(Graphics2D drawing, WeightedNode city) {
        Point center = positions.get(city);
        Color color = LINE;
        if (progress.route().contains(city)) {
            color = ROUTE;
        } else if (city == progress.current()) {
            color = CURRENT;
        } else if (progress.queue().contains(city)) {
            color = QUEUED;
        } else if (progress.visited().contains(city)) {
            color = VISITED;
        }
        int x = center.x - CITY_WIDTH / 2;
        int y = center.y - CITY_HEIGHT / 2;
        drawing.setColor(mix(Color.WHITE, color, 0.11));
        drawing.fillRoundRect(x, y, CITY_WIDTH, CITY_HEIGHT, 15, 15);
        drawing.setColor(color);
        drawing.setStroke(new BasicStroke(2f));
        drawing.drawRoundRect(x, y, CITY_WIDTH, CITY_HEIGHT, 15, 15);

        drawing.setColor(INK);
        drawing.setFont(getFont().deriveFont(Font.BOLD, 15f));
        centerText(drawing, city.getName(), center.x, center.y - 8);
        drawing.setColor(MUTED);
        drawing.setFont(getFont().deriveFont(12f));
        Long g = progress.distances().get(city);
        centerText(drawing, g == null ? "g = —" : "g = " + g + " km", center.x, center.y + 10);
        if (city == start || city == goal) {
            String tag = city == start && city == goal ? "START / GOAL" : city == start ? "START" : "GOAL";
            drawing.setFont(getFont().deriveFont(Font.BOLD, 9f));
            drawing.setColor(INK);
            centerText(drawing, tag, center.x, center.y + 25);
        }
    }
}
