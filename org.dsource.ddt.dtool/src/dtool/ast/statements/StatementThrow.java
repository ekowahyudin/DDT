package dtool.ast.statements;

import melnorme.utilbox.tree.TreeVisitor;
import dtool.ast.IASTVisitor;
import dtool.ast.SourceRange;
import dtool.ast.expressions.Resolvable;

public class StatementThrow extends Statement {
	
	public final Resolvable exp;
	
	public StatementThrow(Resolvable exp, SourceRange sourceRange) {
		initSourceRange(sourceRange);
		this.exp = parentize(exp);
	}
	
	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, exp);
		}
		visitor.endVisit(this);
	}
	
}