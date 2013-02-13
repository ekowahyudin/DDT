package dtool.ast.definitions;

import melnorme.utilbox.tree.TreeVisitor;
import dtool.ast.IASTVisitor;
import dtool.refmodel.IScopeNode;
import dtool.refmodel.pluginadapters.IModuleResolver;

public class TemplateParamAlias extends TemplateParameter {
	
	public TemplateParamAlias(DefUnitTuple dudt) {
		super(dudt);
	}
	
	@Override
	public EArcheType getArcheType() {
		return EArcheType.Alias;
	}
	
	@Override
	public IScopeNode getMembersScope(IModuleResolver moduleResolver) {
		// TODO return intrinsic universal
		return null;
	}
	
	
	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, defname);
		}
		visitor.endVisit(this);
	}
}