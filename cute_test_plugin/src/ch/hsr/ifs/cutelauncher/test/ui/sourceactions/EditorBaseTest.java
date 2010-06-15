/*******************************************************************************
 * Copyright (c) 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cutelauncher.test.ui.sourceactions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.testplugin.Accessor;
import org.eclipse.cdt.ui.testplugin.DisplayHelper;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.cdt.ui.tests.BaseUITestCase;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDE;

import ch.hsr.ifs.cutelauncher.test.TestPlugin;

/**
 * @author Emanuel Graf IFS
 *
 */
public class EditorBaseTest extends BaseUITestCase {
	
	protected static final int INDEXER_WAIT_TIME = 8000;
	protected ICProject cProject;
	protected IIndex index;

	/**
	 * 
	 */
	public EditorBaseTest() {
	}

	/**
	 * @param name
	 */
	public EditorBaseTest(String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		cProject= CProjectHelper.createCCProject("cuteTest", "bin", IPDOMManager.ID_FAST_INDEXER);
		CCorePlugin.getIndexManager().joinIndexer(INDEXER_WAIT_TIME, npm());
		index= CCorePlugin.getIndexManager().getIndex(cProject);
	}
	
	@Override
	protected void tearDown() throws Exception {
		
		closeAllEditors();
		index = null;
		if (cProject != null) {
			CProjectHelper.delete(cProject);
			cProject = null;
		}
		super.tearDown();
	}
	
	protected IProject getProject() {
		return cProject.getProject();
	}
	
	@SuppressWarnings("restriction")
	protected CEditor openEditor(IFile file) throws PartInitException {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		CEditor editor= (CEditor) IDE.openEditor(page, file);
		EditorTestHelper.joinReconciler(EditorTestHelper.getSourceViewer(editor), 100, 500, 10);
		return editor;
	}
	
	public StringBuffer[] getContentsForTest(int sections) throws IOException {
		return TestSourceReader.getContentsForTest(TestPlugin.getDefault().getBundle(), "src", getClass(), getName(), sections);
	}

	protected void type(char character, int keyCode, int stateMask, CEditor cEditor) {
		StyledText textWidget = cEditor.getViewer().getTextWidget();
		assertNotNull(textWidget);
		Accessor accessor= new Accessor(textWidget, StyledText.class);
		Event event= new Event();
		event.character= character;
		event.keyCode= keyCode;
		event.stateMask= stateMask;
		accessor.invoke("handleKeyDown", new Object[] {event});
		
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return false;
			}
		}.waitForCondition(EditorTestHelper.getActiveDisplay(), 200);
		
	}
	
	protected String getCodeFromIFile(IFile file) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(file.getContents()));
		StringBuilder code = new StringBuilder();
		String line;
		while((line = br.readLine()) != null) {
			code.append(line);
			code.append('\n');
		}
		br.close();
		return code.toString();
	}


	protected void executeCommand(String commandId) throws ExecutionException, NotDefinedException, NotEnabledException,
			NotHandledException {
				IHandlerService hs  = (IHandlerService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IHandlerService.class);
				hs.executeCommand(commandId, null);
			}

}
