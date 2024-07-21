import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class MetricsPanel extends JPanel {
    private JTable table;
    private NonEditableTableModel tableModel;
    private JButton uploadButton;

    public MetricsPanel() {
        setLayout(new BorderLayout());

        // Initialize table model with column names
        String[] columnNames = {"Class Name", "Lines", "LOC", "eLOC", "iLOC", "Abstraction", "Instability", "Distance"};
        tableModel = new NonEditableTableModel(columnNames, 0);
        table = new JTable(tableModel);

        add(new JScrollPane(table), BorderLayout.CENTER);

        uploadButton = new JButton("Upload Java Files");
        uploadButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File[] selectedFiles = fileChooser.getSelectedFiles();
                calculateMetrics(selectedFiles);
            }
        });
        add(uploadButton, BorderLayout.NORTH);
    }

    private void calculateMetrics(File[] files) {
        JavaFileParser parser = new JavaFileParser();
        MetricCalculator calculator = new MetricCalculator();
        
        for (File file : files) {
            List<ClassOrInterfaceDeclaration> classes = parser.parseFile(file);
            if (classes != null) {
                for (ClassOrInterfaceDeclaration cls : classes) {
                    try {
                        int lines = calculator.calculateLines(file);
                        int loc = calculator.calculateLOC(file);
                        int eloc = calculator.calculateELOC(file);
                        int iloc = calculator.calculateILOC(file);
                        int abstraction = calculator.calculateAbstraction(cls);
                        double instability = calculator.calculateInstability(classes); // Placeholder method
                        double distance = calculator.calculateDistance(abstraction, instability); // Placeholder method

                        Object[] row = {cls.getName().asString(), lines, loc, eloc, iloc, abstraction, instability, distance};
                        tableModel.addRow(row);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "No classes found in the file: " + file.getName());
            }
        }
    }
}

class NonEditableTableModel extends DefaultTableModel {
    public NonEditableTableModel(Object[] columnNames, int rowCount) {
        super(columnNames, rowCount);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
