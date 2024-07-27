import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

public class MetricsGraphPanel extends JPanel {
    int marg = 50;
    JTable table;
    double test_data_A[] = { 0, 0, 1, 0 };
    double test_data_I[] = { 1, 0.5, 0.134, 0 };

    public MetricsGraphPanel() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        // Axis Labels
        JLabel xlabel = new JLabel("Instability");
        xlabel.setHorizontalAlignment(SwingConstants.RIGHT);
        xlabel.setVerticalAlignment(SwingConstants.CENTER);
        add(xlabel, BorderLayout.PAGE_END);

        JLabel ylabel = new JLabel("Abstractness");
        ylabel.setHorizontalAlignment(SwingConstants.LEFT);
        ylabel.setVerticalAlignment(SwingConstants.TOP);
        add(ylabel, BorderLayout.CENTER);

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Variables
        Graphics2D graph = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();

        // Rendering
        graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw X-Axis & Y-Axis
        graph.draw(new Line2D.Double(marg, marg, marg, height - marg));
        graph.draw(new Line2D.Double(marg, height - marg, width - marg, height - marg));

        graph.drawString("1.0", width - marg - 15, height - marg + 15);
        graph.drawString("0.0", marg - 25, height - marg + 15);
        graph.drawString("1.0", marg - 25, marg + 10);

        // Draw Points
        int i;
        graph.setPaint(Color.RED);
        for (i = 0; i < test_data_A.length; i++) {
            double A = test_data_A[i];
            double I = test_data_I[i];
            graph.fill(new Ellipse2D.Double(scalePointI(I), scalePointA(A), 10, 10));
        }

    }

    private double scalePointA(double num) {
        num = 1 - num; // Inverse the Y direction
        return (num * (getHeight() - (marg * 2) - 10)) + marg;
    }

    private double scalePointI(double num) {
        return (num * (getWidth() - (marg * 2) - 10)) + marg;
    }
}
