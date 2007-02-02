/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule für Technik  
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Emanuel Graf - initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cutelauncher.ui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.hsr.ifs.cutelauncher.model.TestCase;
import ch.hsr.ifs.cutelauncher.model.TestElement;

/**
 * @author Emanuel Graf
 *
 */
public class CuteCompareResultDialog extends TrayDialog {
	
	private static class CompareElement implements ITypedElement {
	    private String fContent;
	    
	    public CompareElement(String content) {
	        fContent= content;
	    }
	    public String getName() {
	        return "<no name>"; //$NON-NLS-1$
	    }
	    public Image getImage() {
	        return null;
	    }
	    public String getType() {
	        return "txt"; //$NON-NLS-1$
	    }
	    public InputStream getContents() {
		    try {
		        return new ByteArrayInputStream(fContent.getBytes("UTF-8")); //$NON-NLS-1$
		    } catch (UnsupportedEncodingException e) {
		        return new ByteArrayInputStream(fContent.getBytes());
		    }
	    }
        public String getCharset() throws CoreException {
            return "UTF-8"; //$NON-NLS-1$
        }
	}
	
	
	private TextMergeViewer compareViewer;
    private String expected = "expected";
    private String actual = "actual";
    private TestCase test;

	public CuteCompareResultDialog(Shell shell, TestCase test) {
		super(shell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX);
		this.test = test;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final CompareConfiguration compareConfiguration= new CompareConfiguration();
		compareConfiguration.setLeftEditable(false);
		compareConfiguration.setLeftLabel("Expected");
		compareConfiguration.setRightEditable(false);
		compareConfiguration.setRightLabel("Actual");
		compareViewer = new TextMergeViewer(parent, compareConfiguration);
		setCompareViewerInput(test);
		return compareViewer.getControl();
	}
	
	public void setCompareViewerInput(TestCase test) {
		if (! compareViewer.getControl().isDisposed())
			compareViewer.setInput(new DiffNode(new CompareElement(expected), new CompareElement(actual)));
	}
	
	

	
}
