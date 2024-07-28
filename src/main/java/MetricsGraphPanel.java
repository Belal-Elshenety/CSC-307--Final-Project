import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MetricsGraphPanel extends JPanel implements PropertyChangeListener {
    private static final int MARGIN = 50;
    private NonEditableTableModel dataTable;
    private final List<Point> points = new ArrayList<>();

    public MetricsGraphPanel() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        // X-Axis Label
        JLabel xlabel = new JLabel("Instability");
        xlabel.setHorizontalAlignment(SwingConstants.RIGHT);
        xlabel.setVerticalAlignment(SwingConstants.CENTER);
        add(xlabel, BorderLayout.PAGE_END);

        // Y-Axis Label
        JLabel ylabel = new JLabel("Abstractness");
        ylabel.setHorizontalAlignment(SwingConstants.LEFT);
        ylabel.setVerticalAlignment(SwingConstants.TOP);
        add(ylabel, BorderLayout.LINE_START);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D graph = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();

        graph.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw X-Axis and Y-Axis
        graph.draw(new Line2D.Double(MARGIN, MARGIN, MARGIN, height - MARGIN));
        graph.draw(new Line2D.Double(MARGIN, height - MARGIN, width - MARGIN, height - MARGIN));

        graph.drawString("1.0", width - MARGIN - 15, height - MARGIN + 15);
        graph.drawString("0.0", MARGIN - 25, height - MARGIN + 15);
        graph.drawString("1.0", MARGIN - 25, MARGIN + 10);

        if (dataTable == null) {
            return;
        }

        // Draw Points
        for (Point p : points) {
            p.paintComponent(graph);
        }

        revalidate();
        repaint();
    }

    private void assembleData() {
        if (dataTable == null) {
            return;
        }

        // Clear existing points
        removeAllPoints();

        // Add new points
        for (int i = 0; i < dataTable.getRowCount(); i++) {
            String name = (String) dataTable.getValueAt(i, 0);
            int eLoc = (int) dataTable.getValueAt(i, 3);
            int maxCC = (int) dataTable.getValueAt(i, 5);
            double A = (double) dataTable.getValueAt(i, 6);
            double I = (double) dataTable.getValueAt(i, 7);

            double scaledX = scalePointI(I);
            double scaledY = scalePointA(A);

            Point p = new Point(name, eLoc, maxCC, scaledX, scaledY, this);
            points.add(p);
            add(p);
        }
    }

    private void removeAllPoints() {
        for (Point p : points) {
            remove(p);
        }
        points.clear();
    }

    private double scalePointA(double num) {
        num = 1 - num; // Inverse the Y direction
        return (num * (getHeight() - (MARGIN * 2) - 10)) + MARGIN;
    }

    private double scalePointI(double num) {
        return (num * (getWidth() - (MARGIN * 2) - 10)) + MARGIN;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        dataTable = (NonEditableTableModel) evt.getNewValue();
        assembleData();
        repaint();
    }
}

class Point extends JComponent {
    private static final int SIZE = 10;
    private final String name;
    private final int eLoc, maxCC;
    private final double x, y;
    private final MetricsGraphPanel graph;

    public Point(String name, int eLoc, int maxCC, double x, double y, MetricsGraphPanel g) {
        this.name = name;
        this.eLoc = eLoc;
        this.maxCC = maxCC;
        this.x = x;
        this.y = y;
        graph = g;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        g2D.setPaint(getComplexityColor(maxCC));
        g2D.fill(new Ellipse2D.Double(x, y, SIZE, SIZE));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(SIZE, SIZE);
    }

    private Color getComplexityColor(int cc) {
        if (cc <= 10) {
            return Color.GREEN;
        } else if (cc <= 20) {
            return Color.YELLOW;
        } else if (cc <= 40) {
            return Color.RED;
        } else {
            return Color.BLACK;
        }
    }

    public void displayData() {
        JLabel data = new JLabel(name + "\neLoc: " + eLoc + "\n");
        graph.add(data);
    }
}
