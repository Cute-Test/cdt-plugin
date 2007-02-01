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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import ch.hsr.ifs.cutelauncher.CuteLauncherPlugin;
import ch.hsr.ifs.cutelauncher.model.TestCase;
import ch.hsr.ifs.cutelauncher.model.TestStatus;

/**
 * @author egraf
 *
 */
public class CuteTestDClickListener implements IDoubleClickListener {

	public void doubleClick(DoubleClickEvent event) {
		if (event.getSelection() instanceof TreeSelection) {
			TreeSelection treeSel = (TreeSelection) event.getSelection();
			if (treeSel.getFirstElement() instanceof TestCase) {
				TestCase tCase = (TestCase) treeSel.getFirstElement();
				if(tCase.getStatus() == TestStatus.failure) {
					openEditor(tCase.getFile(), tCase.getLineNumber());
				}
			}
		}

	}
	
	private void openEditor(IFile file, int lineNumber) {
		IWorkbenchWindow window = CuteLauncherPlugin.getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				try {
					IEditorPart editorPart = page.openEditor(new FileEditorInput(file), getEditorId(file) , false);
					if (lineNumber > 0 && editorPart instanceof ITextEditor) {
						ITextEditor textEditor = (ITextEditor)editorPart;
						IEditorInput input = editorPart.getEditorInput();
						IDocumentProvider provider = textEditor.getDocumentProvider();
						try {
							provider.connect(input);
						} catch (CoreException e) {
							// unable to link
							CuteLauncherPlugin.log(e);
							return;
						}
						IDocument document = provider.getDocument(input);
						try {
							IRegion region= document.getLineInformation(lineNumber - 1);
							textEditor.selectAndReveal(region.getOffset(), region.getLength());
						} catch (BadLocationException e) {
							// unable to link
							CuteLauncherPlugin.log(e);
						}
						provider.disconnect(input);
					}
				} catch (PartInitException e) {
					CuteLauncherPlugin.log(e);
				}	
			}
		}
	}
	
	private String getEditorId(IFile file) {
			IWorkbench workbench= CuteLauncherPlugin.getDefault().getWorkbench();
			// If there is a registered editor for the file use it.
			IEditorDescriptor desc = workbench.getEditorRegistry().getDefaultEditor(file.getName(), getFileContentType(file));
			if (desc == null) {
				//default editor
				desc= workbench.getEditorRegistry().findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
			}
		return desc.getId();
	}

    private IContentType getFileContentType(IFile file) {
        try {
            IContentDescription description= file.getContentDescription();
            if (description != null) {
                return description.getContentType();
            }
        } catch (CoreException e) {
        }
        return null;
    }

}
