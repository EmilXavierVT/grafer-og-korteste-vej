package grafer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.Timer;

public final class RouteLabPanel extends JPanel {
    private final RoadMapPanel roads = new RoadMapPanel();
    private final MazePanel maze = new MazePanel();
    private final JComboBox<String> roadStart = picker(roads.cityNames(), RoadMapPanel.DEFAULT_START);
    private final JComboBox<String> roadGoal = picker(roads.cityNames(), RoadMapPanel.DEFAULT_GOAL);
    private final JComboBox<String> mouseStart = picker(maze.cityNames(), MazePanel.DEFAULT_START);
    private final JComboBox<String> cheese = picker(maze.cityNames(), MazePanel.DEFAULT_GOAL);
    private final JTabbedPane tabs = new JTabbedPane();
    private final JTextArea status = new JTextArea(5, 0);
    private final JTextArea queue = new JTextArea();
    private final JLabel rule = new JLabel();
    private final JButton step = new JButton("Step");
    private final JButton play = new JButton("Play");
    private final JButton reset = new JButton("Reset");
    private final Timer timer = new Timer(320, event -> advance());

    public RouteLabPanel() {
        super(new BorderLayout(14, 12));
        setBackground(new Color(239, 244, 250));
        setBorder(BorderFactory.createEmptyBorder(14, 16, 16, 16));

        JPanel header = new JPanel(new BorderLayout(0, 2));
        header.setOpaque(false);
        header.add(heading("Route Lab", 25), BorderLayout.NORTH);
        JLabel intro = new JLabel("Explore how Dijkstra and A* find the shortest path.");
        intro.setForeground(SearchPanel.MUTED);
        header.add(intro, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        tabs.addTab("Dijkstra  /  Roads", mapTab(roads, "From", roadStart, "To", roadGoal, false));
        tabs.addTab("A*  /  Cheese maze", mapTab(maze, "Mouse", mouseStart, "Cheese", cheese, true));
        add(tabs, BorderLayout.CENTER);
        add(sidebar(), BorderLayout.EAST);

        tabs.addChangeListener(event -> {
            timer.stop();
            refresh();
        });
        roadStart.addActionListener(event -> resetSearch());
        roadGoal.addActionListener(event -> resetSearch());
        mouseStart.addActionListener(event -> resetSearch());
        cheese.addActionListener(event -> resetSearch());
        step.addActionListener(event -> advance());
        play.addActionListener(event -> togglePlay());
        reset.addActionListener(event -> resetSearch());
        refresh();
    }

    private JPanel sidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 12));
        sidebar.setPreferredSize(new Dimension(245, 0));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SearchPanel.LINE),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        status.setEditable(false);
        status.setLineWrap(true);
        status.setWrapStyleWord(true);
        status.setForeground(SearchPanel.INK);
        status.setFont(new JLabel().getFont().deriveFont(13f));
        JPanel progress = new JPanel(new BorderLayout(0, 6));
        progress.setOpaque(false);
        progress.add(heading("Search progress", 17), BorderLayout.NORTH);
        progress.add(status, BorderLayout.CENTER);
        sidebar.add(progress, BorderLayout.NORTH);

        queue.setEditable(false);
        queue.setBackground(SearchPanel.PAPER);
        queue.setForeground(SearchPanel.INK);
        queue.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        queue.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane scroll = new JScrollPane(queue);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createLineBorder(SearchPanel.LINE));
        JPanel candidates = new JPanel(new BorderLayout(0, 6));
        candidates.setOpaque(false);
        rule.setForeground(SearchPanel.MUTED);
        candidates.add(rule, BorderLayout.NORTH);
        candidates.add(scroll, BorderLayout.CENTER);
        sidebar.add(candidates, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, 3, 5, 0));
        buttons.setOpaque(false);
        buttons.add(step);
        buttons.add(play);
        buttons.add(reset);
        sidebar.add(buttons, BorderLayout.SOUTH);
        return sidebar;
    }

    private static JPanel mapTab(JPanel map, String startLabel, JComboBox<String> start,
                                  String goalLabel, JComboBox<String> goal, boolean scent) {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel selectors = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        selectors.setBackground(Color.WHITE);
        selectors.add(new JLabel(startLabel));
        selectors.add(start);
        selectors.add(new JLabel(goalLabel));
        selectors.add(goal);
        panel.add(selectors, BorderLayout.NORTH);
        panel.add(map, BorderLayout.CENTER);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 7));
        legend.setBackground(Color.WHITE);
        legend.add(legendItem("Current", SearchPanel.CURRENT));
        legend.add(legendItem("In queue", SearchPanel.QUEUED));
        legend.add(legendItem("Visited", SearchPanel.VISITED));
        legend.add(legendItem("Route", SearchPanel.ROUTE));
        if (scent) {
            legend.add(legendItem("Stronger scent = lower h", new Color(148, 80, 8)));
        }
        panel.add(legend, BorderLayout.SOUTH);
        return panel;
    }

    private static JComboBox<String> picker(String[] names, String selected) {
        JComboBox<String> picker = new JComboBox<>(names);
        picker.setSelectedItem(selected);
        return picker;
    }

    private static JLabel heading(String text, int size) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, size));
        label.setForeground(SearchPanel.INK);
        return label;
    }

    private static JLabel legendItem(String text, Color color) {
        JLabel label = new JLabel("● " + text);
        label.setForeground(color);
        return label;
    }

    private SearchPanel activeSearch() {
        return tabs.getSelectedIndex() == 0 ? roads : maze;
    }

    private void advance() {
        activeSearch().step();
        refresh();
    }

    private void resetSearch() {
        timer.stop();
        if (tabs.getSelectedIndex() == 0) {
            roads.reset((String) roadStart.getSelectedItem(), (String) roadGoal.getSelectedItem());
        } else {
            maze.reset((String) mouseStart.getSelectedItem(), (String) cheese.getSelectedItem());
        }
        refresh();
    }

    private void togglePlay() {
        if (timer.isRunning()) {
            timer.stop();
            refresh();
        } else {
            timer.start();
            advance();
        }
    }

    private void refresh() {
        SearchPanel search = activeSearch();
        boolean finished = search.finished();
        if (finished) {
            timer.stop();
        }
        status.setText(search.status());
        status.setCaretPosition(0);
        rule.setText(search.queueRule());
        queue.setText(search.queueText());
        queue.setCaretPosition(0);
        step.setEnabled(!finished && !timer.isRunning());
        play.setEnabled(!finished);
        play.setText(timer.isRunning() ? "Pause" : "Play");
    }
}
