import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

import javax.swing.table.DefaultTableModel;

public class MetricCalculator extends PropertyChangeSupport {
    private static MetricCalculator instance;
    private static String[] columnNames = { "Class Name", "Lines", "LOC", "eLOC", "iLOC", "MaxCC", "Abstraction",
            "Instability",
            "Distance" };
    private NonEditableTableModel dataTable = new NonEditableTableModel(columnNames, 0);

    private MetricCalculator() {
        super(new Object());
    }

    public static MetricCalculator getInstance() {
        if (instance == null) {
            instance = new MetricCalculator();
        }
        return instance;
    }

    public void addObserver(PropertyChangeListener p) {
        if (instance != null) {
            instance.addPropertyChangeListener(p);
        }
    }

    public NonEditableTableModel getDataTable() {
        return dataTable;
    }

    public void clearTable() {
        dataTable.setRowCount(0);
        firePropertyChange("dataTable", null, dataTable);
    }

    public void calculateClassMetrics(List<ClassOrInterfaceDeclaration> classes,
            Map<String, Set<String>> dependencies) {
        for (ClassOrInterfaceDeclaration cls : classes) {

            String className = cls.getNameAsString();
            int lines = calculateLines(cls);
            int loc = calculateLOC(cls);
            int eloc = calculateELOC(cls);
            int iloc = calculateILOC(cls);
            int maxcc = calculateMaxCC(cls);
            double abstraction = calculateAbstraction(cls);
            double instability = calculateInstability(dependencies, className);
            double distance = calculateDistance(abstraction, instability);

            Object[] row = { className, lines, loc, eloc, iloc, maxcc, abstraction, instability, distance };
            dataTable.addRow(row);
        }
        firePropertyChange("dataTable", null, dataTable);
    }

    private int calculateLines(ClassOrInterfaceDeclaration cls) {
        return cls.toString().split("\n").length;
    }

    private int calculateLOC(ClassOrInterfaceDeclaration cls) {
        int loc = 0;
        String[] lines = cls.toString().split("\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.startsWith("//") && !line.startsWith("/*") && !line.startsWith("*")) {
                loc++;
            }
        }
        return loc;
    }

    private int calculateELOC(ClassOrInterfaceDeclaration cls) {
        int eLOC = 0;
        String[] lines = cls.toString().split("\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("//") && !line.startsWith("/*") && !line.startsWith("*")
                    && !isDelimiterOnly(line)) {
                eLOC++;
            }
        }
        return eLOC;
    }

    private boolean isDelimiterOnly(String line) {
        return line.matches("[{}()\\[\\];,]+");
    }

    public int calculateILOC(ClassOrInterfaceDeclaration cls) {
        int iLOC = 0;
        String[] lines = cls.toString().split("\n");
        for (String line : lines) {
            line = line.trim();
            // Check for non-import statements with semicolons and control structures
            if (!line.startsWith("import") &&
                    (line.endsWith(";") || line.matches("^(for|while).*"))) {
                iLOC++;
            }
        }
        return iLOC;
    }

    public int calculateAbstraction(ClassOrInterfaceDeclaration cls) {
        return cls.isAbstract() ? 1 : 0;
    }

    public int calculateMaxCC(ClassOrInterfaceDeclaration cls) {
        int maxCC = 1;
        List<MethodDeclaration> methods = cls.getMethods();
        for (MethodDeclaration method : methods) {
            int CC = calculateFuncCC(method);
            if (CC > maxCC) {
                maxCC = CC;
            }
        }
        return maxCC;
    }

    public int calculateFuncCC(MethodDeclaration func) {
        int FuncCC = 1;

        IfStmtCounter ifStmtCounter = new IfStmtCounter();
        ifStmtCounter.visit(func, null);
        FuncCC += ifStmtCounter.getCount();

        WhileStmtCounter whileStmtCounter = new WhileStmtCounter();
        whileStmtCounter.visit(func, null);
        FuncCC += whileStmtCounter.getCount();

        CaseStmtCounter caseStmtCounter = new CaseStmtCounter();
        caseStmtCounter.visit(func, null);
        FuncCC += caseStmtCounter.getCount();

        ForStmtCounter forStmtCounter = new ForStmtCounter();
        forStmtCounter.visit(func, null);
        FuncCC += forStmtCounter.getCount();

        return FuncCC;
    }

    public double calculateInstability(Map<String, Set<String>> dependencies, String className) {
        int cout = calculateCe(dependencies, className);
        int cin = calculateCa(dependencies, className);
        if (cin + cout == 0)
            return 0; // To avoid division by zero
        return (double) cout / (cin + cout);
    }

    private int calculateCe(Map<String, Set<String>> dependencies, String className) {
        return dependencies.getOrDefault(className, new HashSet<>()).size();
    }

    private int calculateCa(Map<String, Set<String>> dependencies, String className) {
        int ca = 0;
        for (Map.Entry<String, Set<String>> entry : dependencies.entrySet()) {
            if (entry.getValue().contains(className)) {
                ca++;
                // // Logging which class is being checked
                // System.out.println("Class " + entry.getKey() + " depends on " + className);
            }
        }
        return ca;
    }

    public double calculateDistance(double abstraction, double instability) {
        return Math.abs(abstraction + instability - 1);
    }
}

class NonEditableTableModel extends DefaultTableModel {
    public NonEditableTableModel(Object[] columnNames, int rowCount) {
        super(columnNames, 0);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}