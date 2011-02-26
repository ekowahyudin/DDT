/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package mmrnmhrm.ui.preferences.pages;


import mmrnmhrm.ui.DeePlugin;
import mmrnmhrm.ui.preferences.DeeEditorConfigurationBlock;

import org.eclipse.dltk.ui.preferences.AbstractConfigurationBlockPreferencePage;
import org.eclipse.dltk.ui.preferences.IPreferenceConfigurationBlock;
import org.eclipse.dltk.ui.preferences.OverlayPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


public class DeeEditorPreferencePage extends AbstractConfigurationBlockPreferencePage {
	
	public final static String PAGE_ID = DeePlugin.EXTENSIONS_IDPREFIX+"preferences.Editor";
	
	@Override
	protected void setDescription() {
		setDescription(null);
	}
	
	@Override
	protected Label createDescriptionLabel(Composite parent) {
		return null;
	}
	
	@Override
	protected void setPreferenceStore() {
		setPreferenceStore(DeePlugin.getDefault().getPreferenceStore());
	}
	
	@Override
	protected IPreferenceConfigurationBlock createConfigurationBlock(OverlayPreferenceStore overlayPreferenceStore) {
		return new DeeEditorConfigurationBlock(this, overlayPreferenceStore);
	}
}
