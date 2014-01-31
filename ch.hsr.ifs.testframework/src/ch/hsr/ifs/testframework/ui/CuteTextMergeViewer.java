/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.ui;

import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.internal.MergeSourceViewer;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.PaintManager;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.WhitespaceCharacterPainter;
import org.eclipse.swt.widgets.Composite;

import ch.hsr.ifs.testframework.TestFrameworkPlugin;
import ch.hsr.ifs.testframework.preference.PreferenceConstants;

/**
 * @author Emanuel Graf
 */
@SuppressWarnings("restriction")
public class CuteTextMergeViewer extends TextMergeViewer {

	private WhitespaceCharacterPainter leftWhitespaceCharacterPainter;
	private WhitespaceCharacterPainter rightWhitespaceCharacterPainter;

	public CuteTextMergeViewer(Composite parent, int style, CompareConfiguration configuration) {
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
		if (TestFrameworkPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.SHOW_WHITESPACES)) {
			whitespaceCharPainter = new WhitespaceCharacterPainter(textViewer);
			textViewer.addPainter(whitespaceCharPainter);
		}
	}

	private enum ViewerLocation {
		LEFT, CENTER, RIGHT
	}

	private TextViewer getSourceViewer(ViewerLocation loc) {
		String fieldName = "";
		switch (loc) {
		case LEFT:
			fieldName = "fLeft";
			break;
		case RIGHT:
			fieldName = "fRight";
			break;
		case CENTER:
			fieldName = "fAncestor";
			break;
		}
		try {
			Field field = getClass().getSuperclass().getDeclaredField(fieldName);
			field.setAccessible(true);
			Object instanceField = field.get(this);
			if (instanceField instanceof MergeSourceViewer) {
				MergeSourceViewer viewer = (MergeSourceViewer) instanceField;
				return viewer.getSourceViewer();
			}
		} catch (NoSuchFieldException e) {
			logException(e);
		} catch (SecurityException e) {
			logException(e);
		} catch (IllegalAccessException e) {
			logException(e);
		}
		return null;
	}

	/**
	 * Installs the painter on the editor.
	 */
	private void installPainter() {
		leftWhitespaceCharacterPainter = installPainter(ViewerLocation.LEFT);
		rightWhitespaceCharacterPainter = installPainter(ViewerLocation.RIGHT);
	}

	private WhitespaceCharacterPainter installPainter(ViewerLocation location) {
		TextViewer viewer = getSourceViewer(location);
		if (viewer != null) {
			WhitespaceCharacterPainter painter = new WhitespaceCharacterPainter(viewer);
			viewer.addPainter(painter);
			return painter;
		}
		return null;
	}

	private void uninstallPainter() {
		uninstallPainter(ViewerLocation.LEFT, leftWhitespaceCharacterPainter);
		uninstallPainter(ViewerLocation.RIGHT, rightWhitespaceCharacterPainter);
	}

	private void uninstallPainter(ViewerLocation location, WhitespaceCharacterPainter painter) {
		TextViewer viewer = getSourceViewer(location);
		if (viewer != null) {
			if (painter == null) {
				painter = getWhitespaceCharacterPainter(viewer);
			}
			viewer.removePainter(painter);
		}
	}

	private WhitespaceCharacterPainter getWhitespaceCharacterPainter(Object viewer) {
		try {
			Class<?> viewerClass = Class.forName("org.eclipse.jface.text.TextViewer");
			Field painterMgField = viewerClass.getDeclaredField("fPaintManager");
			painterMgField.setAccessible(true);
			PaintManager pm = (PaintManager) painterMgField.get(viewer);

			Class<? extends PaintManager> classPm = pm.getClass();
			Field painterListField = classPm.getDeclaredField("fPainters");
			painterListField.setAccessible(true);
			List<?> painters = (List<?>) painterListField.get(pm);
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
		TestFrameworkPlugin.log(new Status(IStatus.ERROR, TestFrameworkPlugin.PLUGIN_ID, e.getMessage(), e));

	}

	public void showWhitespaces(boolean show) {
		if (show) {
			installPainter();
		} else {
			uninstallPainter();
		}
		invalidateTextPresentation();
		refresh();
	}

}
