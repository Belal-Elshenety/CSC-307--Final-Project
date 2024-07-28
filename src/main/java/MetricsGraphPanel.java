import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MetricsGraphPanel extends JPanel implements PropertyChangeListener {
    private int marg = 50;
    private NonEditableTableModel dataTable;
    private boolean complexityGradient = false;

    public MetricsGraphPanel() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        // JPanel graphPanel = new JPanel();
        // setLayout(new BorderLayout());
        // add(graphPanel, BorderLayout.WEST);
        // graphPanel.setVisible(true);

        // Axis Labels
        JLabel xlabel = new JLabel("Instability");
        xlabel.setHorizontalAlignment(SwingConstants.RIGHT);
        xlabel.setVerticalAlignment(SwingConstants.CENTER);
        add(xlabel, BorderLayout.PAGE_END);

        JLabel ylabel = new JLabel("Abstractness");
        ylabel.setHorizontalAlignment(SwingConstants.LEFT);
        ylabel.setVerticalAlignment(SwingConstants.TOP);
        add(ylabel, BorderLayout.CENTER);

        // // Settings Panel
        // JPanel settingsPanel = new JPanel();
        // add(settingsPanel, BorderLayout.EAST);
        // settingsPanel.setPreferredSize(new Dimension(50, getHeight()));
        // settingsPanel.setVisible(true);

        // JCheckBox complexityGradBox = new JCheckBox("Complexity Gradient");
        // complexityGradBox.setBounds(100, 100, 50, 50);
        // complexityGradBox.addItemListener(new ItemListener() {
        // public void itemStateChanged(ItemEvent e) {
        // if (e.getStateChange() == 1) {
        // complexityGradient = true;
        // }
        // }
        // });
        // settingsPanel.add(complexityGradBox);

    }

    public void setComplexityGradient(boolean bool) {
        complexityGradient = bool;
        repaint();
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

        if (dataTable == null) {
            return;
        }
        // Draw Points
        int i;
        graph.setPaint(Color.BLACK);
        System.out.println("Drawing Points");
        for (i = 0; i < dataTable.getRowCount(); i++) {
            if (complexityGradient) {
                graph.setPaint(getComplexityColor((int) dataTable.getValueAt(i, 5)));
            }
            double A = (double) dataTable.getValueAt(i, 6);
            double I = (double) dataTable.getValueAt(i, 7);
            graph.fill(new Ellipse2D.Double(scalePointI(I), scalePointA(A), 10, 10));
        }

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

    private double scalePointA(double num) {
        num = 1 - num; // Inverse the Y direction
        return (num * (getHeight() - (marg * 2) - 10)) + marg;
    }

    private double scalePointI(double num) {
        return (num * (getWidth() - (marg * 2) - 10)) + marg;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        dataTable = (NonEditableTableModel) evt.getNewValue();
        repaint();
    }
}
