import java.awt.BorderLayout;
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

            // Metrics Panel
            MetricsPanel metricsPanel = new MetricsPanel();
            tabbedPane.addTab("Metrics", metricsPanel);

            // Graphics Panel
            MetricsGraphPanel metricsGraphPanel = new MetricsGraphPanel();
            tabbedPane.addTab("Graph Visualization", metricsGraphPanel);
            MetricCalculator.getInstance().addObserver(metricsGraphPanel);

            frame.add(tabbedPane, BorderLayout.CENTER);
            frame.setVisible(true);
        });
    }
}
