import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;

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
                // Logging which class is being checked
                System.out.println("Class " + entry.getKey() + " depends on " + className);
            }
        }
        return ca;
    }

//    public Map<String, Set<String>> parseDependencies(List<File> files) throws IOException {
//        // Setting up the type solver for source files
//        TypeSolver javaParserTypeSolver = new JavaParserTypeSolver(new File("src/main/java"));
//
//        // Setting up the JavaSymbolSolver with only one type solver
//        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(javaParserTypeSolver);
//        ParserConfiguration parserConfiguration = new ParserConfiguration().setSymbolResolver(symbolSolver);
//        JavaParser javaParser = new JavaParser(parserConfiguration);
//
//        Map<String, Set<String>> dependencies = new HashMap<>();
//        Set<String> classNames = new HashSet<>();
//
//        // First pass: collect all class names
//        for (File file : files) {
//            CompilationUnit compilationUnit = javaParser.parse(file).getResult().get();
//            List<ClassOrInterfaceDeclaration> classes = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
//            for (ClassOrInterfaceDeclaration cls : classes) {
//                classNames.add(cls.getNameAsString());
//            }
//        }
//
//        System.out.println("Collected class names: " + classNames);
//
//        // Second pass: collect dependencies
//        for (File file : files) {
//            CompilationUnit compilationUnit = javaParser.parse(file).getResult().get();
//            List<ClassOrInterfaceDeclaration> classes = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
//            for (ClassOrInterfaceDeclaration cls : classes) {
//                String className = cls.getNameAsString();
//                dependencies.putIfAbsent(className, new HashSet<>());
//                Set<String> deps = dependencies.get(className);
//
//                // Handling superclass dependencies
//                cls.getExtendedTypes().forEach(extendedType -> {
//                    String parentClass = extendedType.getNameAsString();
//                    if (classNames.contains(parentClass) && !parentClass.equals(className)) {
//                        deps.add(parentClass);
//                        System.out.println("Class " + className + " extends " + parentClass);
//                    }
//                });
//
//                // Handling field dependencies
//                cls.getFields().forEach(field -> {
//                    String varType = field.getElementType().asString();
//                    if (classNames.contains(varType) && !varType.equals(className)) {
//                        deps.add(varType);
//                        System.out.println("Class " + className + " has field of type " + varType);
//                    }
//                });
//
//                // Analyzing methods and constructors for dependencies
//                List<CallableDeclaration<?>> methodsAndConstructors = new ArrayList<>();
//                methodsAndConstructors.addAll(cls.findAll(MethodDeclaration.class));
//                methodsAndConstructors.addAll(cls.findAll(ConstructorDeclaration.class));
//
//                for (CallableDeclaration<?> callable : methodsAndConstructors) {
//                    callable.findAll(VariableDeclarationExpr.class).forEach(varDecl -> {
//                        varDecl.getVariables().forEach(variable -> {
//                            String varType = variable.getType().asString();
//                            if (classNames.contains(varType) && !varType.equals(className)) {
//                                deps.add(varType);
//                                System.out.println("Class " + className + " adds dependency on type " + varType);
//                            }
//                        });
//                    });
//
//                    // Analyzing method calls for dependencies
//                    callable.findAll(MethodCallExpr.class).forEach(call -> {
//                        call.getScope().ifPresent(scope -> {
//                            String dependency = scope.toString();
//                            if (classNames.contains(dependency) && !dependency.equals(className)) {
//                                deps.add(dependency);
//                                System.out.println("Class " + className + " adds dependency on method call to " + dependency);
//                            }
//                        });
//                    });
//                }
//            }
//        }
//
//        System.out.println("Final dependencies: " + dependencies);
//        return dependencies;
//    }
    public Map<String, Set<String>> parseDependencies(List<File> files) throws IOException {
        // Setting up the type solver for source files
        TypeSolver javaParserTypeSolver = new JavaParserTypeSolver(new File("src/main/java"));

        // Setting up the JavaSymbolSolver with only one type solver
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(javaParserTypeSolver);
        ParserConfiguration parserConfiguration = new ParserConfiguration().setSymbolResolver(symbolSolver);
        JavaParser javaParser = new JavaParser(parserConfiguration);

        Map<String, Set<String>> dependencies = new HashMap<>();
        Set<String> classNames = new HashSet<>();

        // First pass: collect all class names
        for (File file : files) {
            CompilationUnit compilationUnit = javaParser.parse(file).getResult().get();
            List<ClassOrInterfaceDeclaration> classes = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
            for (ClassOrInterfaceDeclaration cls : classes) {
                classNames.add(cls.getNameAsString());
            }
        }

        System.out.println("Collected class names: " + classNames);

        // Second pass: collect dependencies
        for (File file : files) {
            CompilationUnit compilationUnit = javaParser.parse(file).getResult().get();
            List<ClassOrInterfaceDeclaration> classes = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
            for (ClassOrInterfaceDeclaration cls : classes) {
                String className = cls.getNameAsString();
                dependencies.putIfAbsent(className, new HashSet<>());
                Set<String> deps = dependencies.get(className);

                // Handling superclass dependencies
                cls.getExtendedTypes().forEach(extendedType -> {
                    String parentClass = extendedType.getNameAsString();
                    if (classNames.contains(parentClass) && !parentClass.equals(className)) {
                        deps.add(parentClass);
                        System.out.println("Class " + className + " extends " + parentClass);
                    }
                });

                // Handling field dependencies
                cls.getFields().forEach(field -> {
                    String varType = field.getElementType().asString();
                    if (classNames.contains(varType) && !varType.equals(className)) {
                        deps.add(varType);
                        System.out.println("Class " + className + " has field of type " + varType);
                    }
                });

                // Analyzing methods and constructors for dependencies
                List<CallableDeclaration<?>> methodsAndConstructors = new ArrayList<>();
                methodsAndConstructors.addAll(cls.findAll(MethodDeclaration.class));
                methodsAndConstructors.addAll(cls.findAll(ConstructorDeclaration.class));

                for (CallableDeclaration<?> callable : methodsAndConstructors) {
                    callable.findAll(VariableDeclarationExpr.class).forEach(varDecl -> {
                        varDecl.getVariables().forEach(variable -> {
                            String varType = variable.getType().asString();
                            if (classNames.contains(varType) && !varType.equals(className)) {
                                deps.add(varType);
                                System.out.println("Class " + className + " adds dependency on type " + varType);
                            }
                        });
                    });

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
        }

        System.out.println("Final dependencies: " + dependencies);
        return dependencies;
    }


    public double calculateDistance(double abstraction, double instability) {
        return Math.abs(abstraction + instability - 1);
    }
}
