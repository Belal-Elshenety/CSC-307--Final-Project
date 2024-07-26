import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MetricCalculator {

    public int calculateLines(ClassOrInterfaceDeclaration cls) {
        return cls.toString().split("\n").length;
    }

    public int calculateLOC(ClassOrInterfaceDeclaration cls) {
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

    public int calculateELOC(ClassOrInterfaceDeclaration cls) {
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
                // Logging which class is being checked
                System.out.println("Class " + entry.getKey() + " depends on " + className);
            }
        }
        return ca;
    }

    public Map<String, Set<String>> parseDependencies(List<File> files) throws IOException {
        TypeSolver javaParserTypeSolver = new JavaParserTypeSolver(new File("src/main/java"));

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(javaParserTypeSolver);
        ParserConfiguration parserConfiguration = new ParserConfiguration().setSymbolResolver(symbolSolver);
        JavaParser javaParser = new JavaParser(parserConfiguration);

        Set<String> classNames = collectClassNames(files, javaParser);
        System.out.println("Collected class names: " + classNames);

        Map<String, Set<String>> dependencies = new HashMap<>();
        for (File file : files) {
            CompilationUnit compilationUnit = javaParser.parse(file).getResult().get();
            List<ClassOrInterfaceDeclaration> classes = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
            for (ClassOrInterfaceDeclaration cls : classes) {
                processClassDependencies(cls, classNames, dependencies);
            }
        }

        System.out.println("Final dependencies: " + dependencies);
        return dependencies;
    }

    private Set<String> collectClassNames(List<File> files, JavaParser javaParser) throws IOException {
        Set<String> classNames = new HashSet<>();
        for (File file : files) {
            CompilationUnit compilationUnit = javaParser.parse(file).getResult().get();
            List<ClassOrInterfaceDeclaration> classes = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
            for (ClassOrInterfaceDeclaration cls : classes) {
                classNames.add(cls.getNameAsString());
            }
        }
        return classNames;
    }

    private void processClassDependencies(ClassOrInterfaceDeclaration cls, Set<String> classNames,
            Map<String, Set<String>> dependencies) {
        String className = cls.getNameAsString();
        dependencies.putIfAbsent(className, new HashSet<>());
        Set<String> deps = dependencies.get(className);

        handleSuperclassDependencies(cls, classNames, className, deps);
        handleFieldDependencies(cls, classNames, className, deps);
        handleMethodAndConstructorDependencies(cls, classNames, className, deps);
    }

    private void handleSuperclassDependencies(ClassOrInterfaceDeclaration cls, Set<String> classNames, String className,
            Set<String> deps) {
        cls.getExtendedTypes().forEach(extendedType -> {
            String parentClass = extendedType.getNameAsString();
            if (classNames.contains(parentClass) && !parentClass.equals(className)) {
                deps.add(parentClass);
                System.out.println("Class " + className + " extends " + parentClass);
            }
        });
    }

    private void handleFieldDependencies(ClassOrInterfaceDeclaration cls, Set<String> classNames, String className,
            Set<String> deps) {
        cls.getFields().forEach(field -> {
            String varType = field.getElementType().asString();
            if (classNames.contains(varType) && !varType.equals(className)) {
                deps.add(varType);
                System.out.println("Class " + className + " has field of type " + varType);
            }
        });
    }

    private void handleMethodAndConstructorDependencies(ClassOrInterfaceDeclaration cls, Set<String> classNames,
            String className, Set<String> deps) {
        List<CallableDeclaration<?>> methodsAndConstructors = new ArrayList<>();
        methodsAndConstructors.addAll(cls.findAll(MethodDeclaration.class));
        methodsAndConstructors.addAll(cls.findAll(ConstructorDeclaration.class));

        for (CallableDeclaration<?> callable : methodsAndConstructors) {
            handleParameterDependencies(callable, classNames, className, deps);

            // Analyzing method calls for dependencies
            callable.findAll(MethodCallExpr.class).forEach(call -> {
                call.getScope().ifPresent(scope -> {
                    String dependency = scope.toString();
                    if (classNames.contains(dependency) && !dependency.equals(className)) {
                        deps.add(dependency);
                        System.out.println("Class " + className + " adds dependency on method call to " + dependency);
                    }
                });
            });

            // Analyzing object creation for dependencies
            callable.findAll(ObjectCreationExpr.class).forEach(creation -> {
                String createdType = creation.getType().asString();
                if (classNames.contains(createdType) && !createdType.equals(className)) {
                    deps.add(createdType);
                    System.out.println("Class " + className + " creates object of type " + createdType);
                }
            });
        }
    }

    private void handleParameterDependencies(CallableDeclaration<?> callable, Set<String> classNames, String className,
            Set<String> deps) {
        callable.getParameters().forEach(parameter -> {
            String paramType = parameter.getType().asString();
            if (classNames.contains(paramType) && !paramType.equals(className)) {
                deps.add(paramType);
                System.out.println("Class " + className + " has parameter of type " + paramType);
            }
        });
    }

    public double calculateDistance(double abstraction, double instability) {
        return Math.abs(abstraction + instability - 1);
    }
}
