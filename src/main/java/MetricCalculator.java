import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MetricCalculator {

    public int calculateLines(ClassOrInterfaceDeclaration cls) {
        return cls.toString().split("\n").length;
    }

    public int calculateLOC(ClassOrInterfaceDeclaration cls) {
        int loc = 0;
        String[] lines = cls.toString().split("\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("//") && !line.startsWith("/*") && !line.startsWith("*")) {
                loc++;
            }
        }
        return loc;
    }

    public int calculateELOC(ClassOrInterfaceDeclaration cls) {
        int eLOC = 0;
        String[] lines = cls.toString().split("\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("//") && !line.startsWith("/*") && !line.startsWith("*") && !line.equals("{") && !line.equals("}")) {
                eLOC++;
            }
        }
        return eLOC;
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

    public double calculateInstability(Map<String, Set<String>> dependencies, String className) {
        int cout = calculateCe(dependencies, className);
        int cin = calculateCa(dependencies, className);
        if (cin + cout == 0) return 0; // To avoid division by zero
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
            }
        }
        return ca;
    }

    public Map<String, Set<String>> parseDependencies(List<File> files) throws IOException {
        Map<String, Set<String>> dependencies = new HashMap<>();
        JavaParser javaParser = new JavaParser();
        for (File file : files) {
            CompilationUnit compilationUnit = javaParser.parse(file).getResult().get();
            List<ClassOrInterfaceDeclaration> classes = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
            for (ClassOrInterfaceDeclaration cls : classes) {
                String className = cls.getNameAsString();
                Set<String> deps = new HashSet<>();
                compilationUnit.findAll(MethodDeclaration.class).forEach(method -> {
                    method.findAll(com.github.javaparser.ast.expr.MethodCallExpr.class).forEach(call -> {
                        String dependency = call.getScope().map(Object::toString).orElse("");
                        if (!dependency.isEmpty() && !dependency.equals(className)) {
                            deps.add(dependency);
                        }
                    });
                });
                dependencies.put(className, deps);
            }
        }
        return dependencies;
    }

    public double calculateDistance(double abstraction, double instability) {
        return Math.abs(abstraction + instability - 1);
    }
}
