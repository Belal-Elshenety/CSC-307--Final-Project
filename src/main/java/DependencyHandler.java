import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

public abstract class DependencyHandler {

    public static Map<String, Set<String>> parseDependencies(List<File> files) throws IOException {
        TypeSolver javaParserTypeSolver = new JavaParserTypeSolver(new File("src/main/java"));

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(javaParserTypeSolver);
        ParserConfiguration parserConfiguration = new ParserConfiguration().setSymbolResolver(symbolSolver);
        JavaParser javaParser = new JavaParser(parserConfiguration);

        Set<String> classNames = collectClassNames(files, javaParser);

        Map<String, Set<String>> dependencies = new HashMap<>();
        for (File file : files) {
            CompilationUnit compilationUnit = javaParser.parse(file).getResult().get();
            List<ClassOrInterfaceDeclaration> classes = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
            for (ClassOrInterfaceDeclaration cls : classes) {
                processClassDependencies(cls, classNames, dependencies);
            }
        }
        return dependencies;
    }

    private static Set<String> collectClassNames(List<File> files, JavaParser javaParser) throws IOException {
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

    private static void processClassDependencies(ClassOrInterfaceDeclaration cls, Set<String> classNames,
            Map<String, Set<String>> dependencies) {
        String className = cls.getNameAsString();
        dependencies.putIfAbsent(className, new HashSet<>());
        Set<String> deps = dependencies.get(className);

        handleSuperclassDependencies(cls, classNames, className, deps);
        handleFieldDependencies(cls, classNames, className, deps);
        handleMethodAndConstructorDependencies(cls, classNames, className, deps);
    }

    private static void handleSuperclassDependencies(ClassOrInterfaceDeclaration cls, Set<String> classNames,
            String className,
            Set<String> deps) {
        cls.getExtendedTypes().forEach(extendedType -> {
            String parentClass = extendedType.getNameAsString();
            if (classNames.contains(parentClass) && !parentClass.equals(className)) {
                deps.add(parentClass);
            }
        });
    }

    private static void handleFieldDependencies(ClassOrInterfaceDeclaration cls, Set<String> classNames,
            String className,
            Set<String> deps) {
        cls.getFields().forEach(field -> {
            String varType = field.getElementType().asString();
            if (classNames.contains(varType) && !varType.equals(className)) {
                deps.add(varType);
            }
        });
    }

    private static void handleMethodAndConstructorDependencies(ClassOrInterfaceDeclaration cls, Set<String> classNames,
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
                    }
                });
            });

            // Analyzing object creation for dependencies
            callable.findAll(ObjectCreationExpr.class).forEach(creation -> {
                String createdType = creation.getType().asString();
                if (classNames.contains(createdType) && !createdType.equals(className)) {
                    deps.add(createdType);
                }
            });
        }
    }

    private static void handleParameterDependencies(CallableDeclaration<?> callable, Set<String> classNames,
            String className,
            Set<String> deps) {
        callable.getParameters().forEach(parameter -> {
            String paramType = parameter.getType().asString();
            if (classNames.contains(paramType) && !paramType.equals(className)) {
                deps.add(paramType);
            }
        });
    }
}
