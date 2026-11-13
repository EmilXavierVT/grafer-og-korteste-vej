package grafer;

import grafer.ui.RouteLabPanel;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Route Lab");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setContentPane(new RouteLabPanel());
            window.pack();
            window.setMinimumSize(new Dimension(800, 560));
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}
