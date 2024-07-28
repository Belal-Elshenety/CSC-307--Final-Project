import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.event.ItemListener;

public class SettingsPanel extends JPanel {
    public SettingsPanel(MetricsGraphPanel graph) {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel f = new JPanel();
        JCheckBox complexityGradBox = new JCheckBox("Complexity Gradient", false);
        complexityGradBox.setLocation(100, 100);
        f.add(complexityGradBox);
        complexityGradBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == 1) {
                    graph.setComplexityGradient(true);
                } else {
                    graph.setComplexityGradient(false);
                }
            }
        });

        // JCheckBox default0 = new JCheckBox("default0");
        // default0.setLocation(100, 200);
        // f.add(default0);

        // JCheckBox default01 = new JCheckBox("default0");
        // default01.setLocation(100, 200);
        // f.add(default01);

        // JCheckBox default02 = new JCheckBox("default0");
        // default02.setLocation(100, 200);
        // f.add(default02);

        // JCheckBox default03 = new JCheckBox("default0");
        // default03.setLocation(100, 200);
        // f.add(default03);

        // JCheckBox default04 = new JCheckBox("default0");
        // default04.setLocation(100, 200);
        // f.add(default04);

        f.setVisible(true);
        add(f, BorderLayout.CENTER);
    }
}
