import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class JavaFileParser {

    public static List<ClassOrInterfaceDeclaration> parseFile(File file) {
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
