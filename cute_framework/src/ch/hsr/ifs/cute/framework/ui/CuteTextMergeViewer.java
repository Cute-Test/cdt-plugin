/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Emanuel Graf - initial API and implementation
 ******************************************************************************/
package ch.hsr.ifs.cute.framework.ui;

import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.PaintManager;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.WhitespaceCharacterPainter;
import org.eclipse.swt.widgets.Composite;

import ch.hsr.ifs.cute.framework.CuteFrameworkPlugin;
import ch.hsr.ifs.cute.framework.preference.PreferenceConstants;

/**
 * @author Emanuel Graf
 */
public class CuteTextMergeViewer extends TextMergeViewer {

	private WhitespaceCharacterPainter leftWhitespaceCharacterPainter;
	private WhitespaceCharacterPainter rightWhitespaceCharacterPainter;



	public CuteTextMergeViewer(Composite parent, int style,
			CompareConfiguration configuration) {
		super(parent, style, configuration);
	}
	
	

	@Override
	protected void createControls(Composite composite) {
		super.createControls(composite);
	}


	@Override
	protected void configureTextViewer(TextViewer textViewer) {
		super.configureTextViewer(textViewer);
		WhitespaceCharacterPainter whitespaceCharPainter = null;
		if(CuteFrameworkPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.SHOW_WHITESPACES)) {
			whitespaceCharPainter= new WhitespaceCharacterPainter(textViewer);
			textViewer.addPainter(whitespaceCharPainter);
		}
	}
	
	private enum ViewerLocation{
		LEFT,
		CENTER,
		RIGHT
	}
	
	private TextViewer getSourceViewer(ViewerLocation loc) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Class<?> class1 = ((TextMergeViewer)this).getClass().getSuperclass();
		String fieldName = ""; //$NON-NLS-1$
		switch (loc) {
		case LEFT:
			fieldName = "fLeft"; //$NON-NLS-1$
			break;
		case RIGHT:
			fieldName = "fRight"; //$NON-NLS-1$
			break;
		case CENTER:
			fieldName = "fAncestor"; //$NON-NLS-1$
			break;
		}
		
		Field field = class1.getDeclaredField(fieldName);
		field.setAccessible(true);
		TextViewer viewer = (TextViewer) field.get(this);
		return viewer;

	}
	
	
	/**
	 * Installs the painter on the editor.
	 */
	private void installPainter() {
		try {
			TextViewer left = getSourceViewer(ViewerLocation.LEFT);
			TextViewer right = getSourceViewer(ViewerLocation.RIGHT);
			
			leftWhitespaceCharacterPainter = new WhitespaceCharacterPainter((ITextViewer) left);
			left.addPainter(leftWhitespaceCharacterPainter);
			rightWhitespaceCharacterPainter = new WhitespaceCharacterPainter((ITextViewer) right);
			right.addPainter(rightWhitespaceCharacterPainter);
		} catch (Exception e) {
			logException(e);
		}
	}

	/**
	 * Remove the painter from the current editor.
	 */
	private void uninstallPainter() {
		try {
			TextViewer left = getSourceViewer(ViewerLocation.LEFT);
			TextViewer right = getSourceViewer(ViewerLocation.RIGHT);
			
			
			if(leftWhitespaceCharacterPainter == null) {
				leftWhitespaceCharacterPainter = getWhitespaceCharacterPainter(left);
			}
			if(rightWhitespaceCharacterPainter == null) {
				rightWhitespaceCharacterPainter = getWhitespaceCharacterPainter(right);
			}
			
			left.removePainter(leftWhitespaceCharacterPainter);
			right.removePainter(rightWhitespaceCharacterPainter);
			left.invalidateTextPresentation();
			right.invalidateTextPresentation();
			left.refresh();
			right.refresh();
			
		} catch (Exception e) {
			logException(e);
		}
		
	}



	@SuppressWarnings("unchecked")
	private WhitespaceCharacterPainter getWhitespaceCharacterPainter(
			Object viewer) {
		try {
			Class<?> viewerClass = Class.forName("org.eclipse.jface.text.TextViewer"); //$NON-NLS-1$
			Field painterMgField = viewerClass.getDeclaredField("fPaintManager"); //$NON-NLS-1$
			painterMgField.setAccessible(true);
			PaintManager pm = (PaintManager)painterMgField.get(viewer);
			
			Class<? extends PaintManager> classPm = pm.getClass();
			Field painterListField = classPm.getDeclaredField("fPainters"); //$NON-NLS-1$
			painterListField.setAccessible(true);
			List painters = (List) painterListField.get(pm);
			for (Object object : painters) {
				if (object instanceof WhitespaceCharacterPainter) {
					WhitespaceCharacterPainter whitePainter = (WhitespaceCharacterPainter) object;
					return whitePainter;
				}
			}
			
			
		} catch (Exception e) {
			logException(e);
		}
		return null;
	}



	private void logException(Exception e) {
		CuteFrameworkPlugin.log(new Status(IStatus.ERROR, CuteFrameworkPlugin.PLUGIN_ID,e.getMessage(), e));
		
	}



	public void showWhitespaces(boolean show) {
		if(show) {
			installPainter();
		}else {
			uninstallPainter();
		}
		invalidateTextPresentation();
		refresh();
	}

}
