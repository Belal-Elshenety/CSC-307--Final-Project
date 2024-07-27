import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

public class MainClass {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Java Metrics and Dependency Graphs");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);

            JTabbedPane tabbedPane = new JTabbedPane();

            JPanel metricsGraphPanel = new MetricsGraphPanel();
            tabbedPane.addTab("Graph Visualization", metricsGraphPanel);

            // Metrics Panel
            MetricsPanel metricsPanel = new MetricsPanel();
            tabbedPane.addTab("Metrics", metricsPanel);

            frame.add(tabbedPane, BorderLayout.CENTER);
            frame.setVisible(true);
        });
    }
}
