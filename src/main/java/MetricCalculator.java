import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

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
            if (!line.isEmpty() && !line.startsWith("//") && !line.startsWith("/*") && !line.startsWith("*")) {
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
                (line.endsWith(";") || line.matches("^(if|for|while|switch|case|try|catch|finally|do|else|return|throw).*"))) {
                iLOC++;
            }
        }
        return iLOC;
    }

    public int calculateAbstraction(ClassOrInterfaceDeclaration cls) {
        return cls.isAbstract() ? 1 : 0;
    }

    public double calculateInstability(List<ClassOrInterfaceDeclaration> classes) {
        // Placeholder for instability calculation
        return 0.0;
    }

    public double calculateDistance(double abstraction, double instability) {
        return Math.abs(abstraction + instability - 1);
    }
}
