/*******************************************************************************
 * Copyright (c) 2014, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package mmrnmhrm.core.workspace.viewmodel;

import static melnorme.utilbox.core.CoreUtil.arrayFrom;

import java.nio.file.Path;
import java.util.ArrayList;

import dtool.dub.DubBundle;
import melnorme.lang.ide.core.project_model.view.AbstractBundleModelElement;
import melnorme.lang.ide.core.project_model.view.BundleModelElementKind;
import melnorme.lang.ide.core.project_model.view.DependenciesContainer;

public class DubDependencyElement extends AbstractBundleModelElement<DependenciesContainer> {
	
	protected final DubBundle dubBundle;
	protected final DubDepSourceFolderElement[] children;
	
	public DubDependencyElement(DependenciesContainer parent, DubBundle dubBundle) {
		super(parent);
		this.dubBundle = dubBundle;
		this.children = createChildren();
	}
	
	@Override
	public BundleModelElementKind getElementType() {
		return BundleModelElementKind.RESOLVED_DEP;
	}
	
	public String getBundleName() {
		return dubBundle.name;
	}
	
	@Override
	public String getElementName() {
		return getBundleName();
	}
	
	@Override
	public String getPathString() {
		return getParent().getPathString() + "/["+getBundleName()+"]";
	}
	
	public DubBundle getDubBundle() {
		return dubBundle;
	}
	
	protected DubDepSourceFolderElement[] createChildren() {
		ArrayList<DubDepSourceFolderElement> sourceContainers = new ArrayList<>();
		
		for (Path localPath : dubBundle.getEffectiveSourceFolders()) {
			sourceContainers.add(new DubDepSourceFolderElement(this, localPath));
		}
		return arrayFrom(sourceContainers, DubDepSourceFolderElement.class);
	}
	
	@Override
	public boolean hasChildren() {
		return children.length > 0;
	}
	
	@Override
	public Object[] getChildren() {
		return children;
	}
	
}