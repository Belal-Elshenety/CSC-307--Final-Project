import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        List<File> fileList = new ArrayList<>();
        for (File file : files) {
            fileList.add(file);
        }

        try {
            Map<String, Set<String>> dependencies = calculator.parseDependencies(fileList);

            for (File file : files) {
                List<ClassOrInterfaceDeclaration> classes = parser.parseFile(file);
                if (classes != null) {
                    for (ClassOrInterfaceDeclaration cls : classes) {
                        String className = cls.getNameAsString();
                        int lines = calculator.calculateLines(cls);
                        int loc = calculator.calculateLOC(cls);
                        int eloc = calculator.calculateELOC(cls);
                        int iloc = calculator.calculateILOC(cls);
                        int abstraction = calculator.calculateAbstraction(cls);
                        double instability = calculator.calculateInstability(dependencies, className);
                        double distance = calculator.calculateDistance(abstraction, instability);

                        Object[] row = {className, lines, loc, eloc, iloc, abstraction, instability, distance};
                        tableModel.addRow(row);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "No classes found in the file: " + file.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while calculating metrics.");
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
