/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.test.framework.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Shell;

import ch.hsr.ifs.test.framework.model.TestCase;

/**
 * @author Emanuel Graf
 *
 */
public class CuteCompareResultAction extends Action {
	
	private TestCase test;
	private CuteCompareResultDialog dialog;
	private Shell shell;
	
	

	public CuteCompareResultAction(TestCase test, Shell shell) {
		super();
		this.test = test;
		this.shell = shell;
	}



	@Override
	public void run() {
		if (dialog != null) {
			dialog.setCompareViewerInput(test);
				
		} else {
			dialog= new CuteCompareResultDialog(shell, test);
			dialog.create();
			dialog.getShell().addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					dialog= null;
				}
			});
			dialog.setBlockOnOpen(false);
			dialog.open();
		}
	}
	

}
