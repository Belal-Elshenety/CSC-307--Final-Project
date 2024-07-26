import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

// Visitor class to count IfStmt instances
class IfStmtCounter extends VoidVisitorAdapter<Void> {
    private int count = 0;

    @Override
    public void visit(IfStmt n, Void arg) {
        super.visit(n, arg);
        count++;
    }

    public int getCount() {
        return count;
    }
}

// Visitor class to count WhileStmt instances
class WhileStmtCounter extends VoidVisitorAdapter<Void> {
    private int count = 0;

    @Override
    public void visit(WhileStmt n, Void arg) {
        super.visit(n, arg);
        count++;
    }

    public int getCount() {
        return count;
    }
}

// Visitor class to count IfStmt instances
class CaseStmtCounter extends VoidVisitorAdapter<Void> {
    private int count = 0;

    @Override
    public void visit(SwitchEntry n, Void arg) {
        super.visit(n, arg);
        count++;
    }

    public int getCount() {
        return count;
    }
}