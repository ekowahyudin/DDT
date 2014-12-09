/*******************************************************************************
 * Copyright (c) 2014, 2014 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package melnorme.lang.tooling.ast;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertFail;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;
import melnorme.lang.tooling.ast.util.NodeElementUtil;
import melnorme.lang.tooling.context.ISemanticContext;
import melnorme.lang.tooling.engine.IElementSemantics;
import melnorme.lang.tooling.engine.PickedElement;
import melnorme.lang.tooling.engine.scoping.CommonScopeLookup;
import melnorme.lang.tooling.engine.scoping.INonScopedContainer;
import melnorme.lang.tooling.symbols.INamedElement;
import dtool.ast.definitions.DefUnit;


public abstract class CommonLanguageElement implements ILanguageElement {
	
	public CommonLanguageElement() {
	}
	
	@Override
	public abstract ILanguageElement getParent();
	
	public INamedElement getParentNamespace() {
		return NodeElementUtil.getOuterNamedElement(this);
	}
	
	/* -----------------  ----------------- */
	
	@Override
	public IElementSemantics getSemantics(ISemanticContext parentContext) {
		ISemanticContext context = getContextForThisElement(parentContext);
		return context.getSemanticsEntry(this);
	}
	
	@Override
	public ISemanticContext getContextForThisElement(ISemanticContext parentContext) {
		return parentContext.findSemanticContext(this);
	}
	
	@Override
	public IElementSemantics createSemantics(PickedElement<?> pickedElement) {
		assertTrue(pickedElement.element == this); // Note this precondition!
		return doCreateSemantics(pickedElement);
	}
	
	@SuppressWarnings("unused")
	protected IElementSemantics doCreateSemantics(PickedElement<?> pickedElement) {
		throw assertFail(); // Not valid unless re-implemented.
	}
	
	@Override
	public void evaluateForScopeLookup(CommonScopeLookup lookup, boolean importsOnly, boolean isSequentialLookup) {
		if(this instanceof INonScopedContainer) {
			INonScopedContainer container = ((INonScopedContainer) this);
			// FIXME: remove need for isSequentialLookup?
			lookup.evaluateScopeElements(container.getMembersIterable(), isSequentialLookup, importsOnly);
		}
		
		if(!importsOnly && this instanceof DefUnit) {
			DefUnit defunit = (DefUnit) this;
			lookup.visitElement(defunit);
		}
	}
	
}