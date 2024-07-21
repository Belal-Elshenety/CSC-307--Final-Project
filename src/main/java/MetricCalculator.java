import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MetricCalculator {
    public int calculateLines(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath());
        return lines.size();
    }

    public int calculateLOC(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath());
        int loc = 0;
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("//") && !line.startsWith("/*") && !line.startsWith("*") ) {
                loc++;
            }
        }
        return loc;
    }

    public int calculateELOC(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath());
        int eLOC = 0;
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("//") && !line.startsWith("/*") && !line.startsWith("*") && !line.equals("{") && !line.equals("}")) {
                eLOC++;
            }
        }
        return eLOC;
    }

    public int calculateILOC(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath());
        int iLOC = 0;
        for (String line : lines) {
            line = line.trim();
            // Check for non-import statements with semicolons and control structures
            if (!line.startsWith("import") && 
                (line.endsWith(";") || line.matches("^(for).*"))) {
                iLOC++;
            }
        }
        return iLOC;
    }

    public int calculateAbstraction(ClassOrInterfaceDeclaration cls) {
        return cls.isAbstract() ? 1 : 0;
    }

    public double calculateInstability(Map<String, Set<String>> dependencies, String className) {
        int ce = calculateCe(dependencies, className);
        int ca = calculateCa(dependencies, className);
        return (double) ce / (ca + ce);
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
