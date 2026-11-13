package grafer.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

abstract class SearchPanel extends JPanel {
    static final int MAP_WIDTH = 600;
    static final int MAP_HEIGHT = 480;
    static final Color INK = new Color(28, 42, 62);
    static final Color MUTED = new Color(82, 100, 122);
    static final Color LINE = new Color(214, 224, 237);
    static final Color PAPER = new Color(248, 250, 253);
    static final Color CURRENT = new Color(34, 91, 199);
    static final Color QUEUED = new Color(115, 75, 171);
    static final Color VISITED = new Color(99, 123, 153);
    static final Color ROUTE = new Color(0, 125, 101);

    private final String queueRule;

    SearchPanel(String queueRule) {
        this.queueRule = queueRule;
        setPreferredSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
        setBackground(PAPER);
    }

    final String queueRule() {
        return queueRule;
    }

    abstract String[] cityNames();
    abstract void reset(String startName, String goalName);
    abstract void step();
    abstract boolean finished();
    abstract String status();
    abstract String queueText();
    abstract void drawBoard(Graphics2D drawing);

    @Override
    protected final void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D drawing = (Graphics2D) graphics.create();
        try {
            double scale = Math.min(getWidth() / (double) MAP_WIDTH, getHeight() / (double) MAP_HEIGHT);
            drawing.translate((getWidth() - MAP_WIDTH * scale) / 2, (getHeight() - MAP_HEIGHT * scale) / 2);
            drawing.scale(scale, scale);
            drawing.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawing.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            drawBoard(drawing);
        } finally {
            drawing.dispose();
        }
    }

    static void centerText(Graphics2D drawing, String text, int x, int baseline) {
        drawing.drawString(text, x - drawing.getFontMetrics().stringWidth(text) / 2, baseline);
    }

    static Color mix(Color first, Color second, double amount) {
        int red = (int) (first.getRed() + (second.getRed() - first.getRed()) * amount);
        int green = (int) (first.getGreen() + (second.getGreen() - first.getGreen()) * amount);
        int blue = (int) (first.getBlue() + (second.getBlue() - first.getBlue()) * amount);
        return new Color(red, green, blue);
    }
}
