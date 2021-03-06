/*******************************************************************************
 * Copyright (c) 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package mmrnmhrm.ui;

import org.eclipse.jface.resource.ImageDescriptor;

import melnorme.lang.ide.ui.LangElementImages;
import melnorme.lang.ide.ui.LangImages;
import melnorme.lang.ide.ui.utils.PluginImagesHelper.ImageHandle;

public abstract class DeeImages extends LangImages {
	
	protected static final String CAT_ELEMS = "language_elements";
	
	protected static final String T_ACTIONS = "action16e";
	
	public static final ImageHandle ELEM_MODULE = LangElementImages.PACKAGE;
	public static final ImageHandle ELEM_PACKAGE = createManaged(CAT_ELEMS, "dee_package.png");

	public static final ImageHandle ENT_ERROR = LangElementImages.ERROR_ELEMENT;
	
	public static final ImageHandle ENT_VARIABLE = LangElementImages.VARIABLE;
	public static final ImageHandle ENT_FUNCTION = LangElementImages.FUNCTION;
	public static final ImageHandle ENT_CONSTRUCTOR = LangElementImages.CONSTRUCTOR;
	
	public static final ImageHandle ENT_NATIVE = LangElementImages.T_NATIVE;
	public static final ImageHandle ENT_STRUCT = LangElementImages.T_STRUCT;
	public static final ImageHandle ENT_CLASS = LangElementImages.T_CLASS;
	public static final ImageHandle ENT_INTERFACE = LangElementImages.T_INTERFACE;
	public static final ImageHandle ENT_ENUM = LangElementImages.T_ENUM;
	public static final ImageHandle ENT_UNION = createManaged(CAT_ELEMS, "t_union.png");
	public static final ImageHandle ENT_TEMPLATE = LangElementImages.NAMESPACE;
	
	public static final ImageHandle ENT_MIXIN = LangElementImages.NAMESPACE;
	public static final ImageHandle ENT_TYPE_PARAMETER = LangElementImages.T_TYPE;
	public static final ImageHandle ENT_TUPLE = createManaged(CAT_ELEMS, "tuple.png");
	
	
	/* ---------- DUB elements ---------- */
	
	protected static final String T_OBJ_DUB = "obj16_dub";
	
	public static final ImageDescriptor DUB_STD_LIB = NAV_LibraryNative;
	public static final ImageDescriptor DUB_BUNDLE_DEP = createUnmanaged(T_OBJ_DUB, "DubBundleDep.png");
	public static final ImageDescriptor DUB_RAW_DEP = createUnmanaged(T_OBJ_DUB, "DubRawDep.png");
	public static final ImageDescriptor DUB_MANIFEST = createUnmanaged(T_OBJ_DUB, "DubManifest.png");
	
	/* ------------------------------------------- */
	
	public static final ImageDescriptor OPEN_DEF_DESC = createUnmanaged(T_ACTIONS, "gotodef.gif");
	
	public static final ImageDescriptor COLLAPSE_ALL = createUnmanaged(T_ACTIONS, "collapseall.gif");
	public static final ImageDescriptor EXPAND_ALL = createUnmanaged(T_ACTIONS, "expandall.gif");
	
	
	/* ------------------------------------------- */
	
	public static ImageDescriptor getIDEInternalErrorImageDescriptor() {
		// BM: maybe there's some other image we could use
		return ImageDescriptor.getMissingImageDescriptor();
	}
	
}