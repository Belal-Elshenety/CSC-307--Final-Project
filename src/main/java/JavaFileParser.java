import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.io.File;
import java.io.IOException;
import java.util.List;

/*
 * Singleton class JavaFileParser used for specific parsing use cases
 */
public class JavaFileParser {
    private static JavaFileParser instance;

    private JavaFileParser() {
        super();
    }

    public static JavaFileParser getInstance() {
        if (instance == null) {
            instance = new JavaFileParser();
        }
        return instance;
    }

    public List<ClassOrInterfaceDeclaration> parseFile(File file) {
        try {
            JavaParser javaParser = new JavaParser();
            ParseResult<CompilationUnit> parseResult = javaParser.parse(file);
            if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
                CompilationUnit compilationUnit = parseResult.getResult().get();
                return compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
            } else {
                System.err.println("Parse errors: " + parseResult.getProblems());
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
