import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MetricsPanel extends JPanel {
    private JTable table;

    public MetricsPanel() {
        setLayout(new BorderLayout());
        table = new JTable(MetricCalculator.getInstance().getDataTable());
        add(new JScrollPane(table), BorderLayout.CENTER);
        setStrictTable();

        JButton uploadButton = new JButton("Upload Java Files");
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

        JButton clearButton = new JButton("Clear Java Files");
        clearButton.addActionListener(l -> {
            MetricCalculator.getInstance().clearTable();
        });
        add(clearButton, BorderLayout.SOUTH);
    }

    private void setStrictTable() {
        // Prevent user from shifting table elements
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.setAutoCreateRowSorter(true);
    }

    private void calculateMetrics(File[] files) {
        List<File> fileList = new ArrayList<>(); // What is the point of this??
        for (File file : files) {
            fileList.add(file);
        }

        try {
            Map<String, Set<String>> dependencies = DependencyHandler.parseDependencies(fileList);

            for (File file : files) {
                List<ClassOrInterfaceDeclaration> classes = JavaFileParser.parseFile(file);
                if (classes != null) {
                    MetricCalculator.getInstance().calculateClassMetrics(classes, dependencies);
                } else {
                    JOptionPane.showMessageDialog(this, "No classes found in the file: " + file.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while calculating metrics.");
        }
    }

    public JTable getDataTable() {
        return table;
    }
}