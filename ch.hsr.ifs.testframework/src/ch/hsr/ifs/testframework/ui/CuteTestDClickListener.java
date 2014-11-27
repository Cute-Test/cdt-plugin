/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.ui;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.debug.core.ILaunchConfiguration;
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

import ch.hsr.ifs.testframework.TestFrameworkPlugin;
import ch.hsr.ifs.testframework.model.TestCase;
import ch.hsr.ifs.testframework.model.TestSession;
import ch.hsr.ifs.testframework.model.TestStatus;

/**
 * @author Emanuel Graf
 * 
 */
public class CuteTestDClickListener implements IDoubleClickListener {

	private static final String REGEX = "::";
	private TestSession session = null;

	public CuteTestDClickListener(TestSession session) {
		super();
		this.session = session;
	}

	public void doubleClick(DoubleClickEvent event) {
		if (event.getSelection() instanceof TreeSelection) {
			TreeSelection treeSel = (TreeSelection) event.getSelection();
			if (treeSel.getFirstElement() instanceof TestCase) {
				TestCase tCase = (TestCase) treeSel.getFirstElement();
				if (tCase.getStatus() == TestStatus.failure) {
					openEditor(tCase.getFile(), tCase.getLineNumber(), false);
				} else {
					openEditorForNonFailingTestCase(tCase.getName());
				}
			}
		}

	}

	public void setSession(TestSession session) {
		this.session = session;
	}

	private void openEditorForNonFailingTestCase(String testCaseName) {
		try {
			ILaunchConfiguration launchConfiguration = session.getLaunch().getLaunchConfiguration();
			String launchConfigName = launchConfiguration.getAttribute("org.eclipse.cdt.launch.PROJECT_ATTR", launchConfiguration.getName());
			ICProject[] projects = CoreModel.getDefault().getCModel().getCProjects();
			for (ICProject project : projects) {
				String projectName = project.getElementName();
				if (!projectName.equals(launchConfigName))
					continue;
				IIndex index = CCorePlugin.getIndexManager().getIndex(project);
				IIndexBinding[] bindings = getBindings(testCaseName, index);
				checkBindingsOpenEditor(index, bindings);
			}
		} catch (CModelException e) {
			TestFrameworkPlugin.log(e);
		} catch (CoreException e) {
			TestFrameworkPlugin.log(e);
		}
	}

	private void checkBindingsOpenEditor(IIndex index, IIndexBinding[] bindings) throws CoreException {
		for (int bi = 0; bindings != null && bi < bindings.length; ++bi) {
			IIndexBinding binding = bindings[bi];
			if (binding == null)
				continue;

			IIndexName[] definition = index.findDefinitions(index.adaptBinding(binding));
			if (definition == null || definition.length == 0)
				continue;
			IASTFileLocation loc = definition[0].getFileLocation();
			IPath filePath = new Path(loc.getFileName());
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(filePath);
			if (file == null)
				continue;
			openEditor(file, loc.getNodeOffset(), true);
			return;
		}
	}

	/**
	 * @since 3.0
	 */
	private IIndexBinding[] getBindings(String testCaseName, IIndex index) throws CoreException {
		IIndexBinding[] bindings;
		boolean fullyQualified = isQualifiedName(testCaseName);
		if (fullyQualified) {
			char[][] namesChar = getNamesArray(testCaseName);
			bindings = index.findBindings(namesChar, IndexFilter.ALL, new NullProgressMonitor());
		} else {
			bindings = index.findBindings(testCaseName.toCharArray(), false, IndexFilter.ALL, new NullProgressMonitor());
		}
		return bindings;
	}

	/**
	 * @since 3.0
	 */
	private char[][] getNamesArray(String testCaseName) {
		String[] names = removeEmptyNames(testCaseName.split(REGEX));
		char[][] namesChar = new char[names.length][];
		for (int j = 0; j < names.length; ++j) {
			namesChar[j] = names[j].toCharArray();
		}
		return namesChar;
	}

	private String[] removeEmptyNames(String[] split) {
		for (int i = 0; i < split.length; i++) {
			if (split[i].length() == 0)
				split[i] = null;
		}
		return ArrayUtil.removeNulls(split);
	}

	private boolean isQualifiedName(String testCaseName) {
		return testCaseName.contains(REGEX);
	}

	private void openEditor(IFile file, int lineNumberOrOffset, boolean isOffset) {
		IWorkbenchPage page = getActivePage();
		if (page != null) {
			try {
				IEditorPart editorPart = page.openEditor(new FileEditorInput(file), getEditorId(file), false);
				if (lineNumberOrOffset > 0 && editorPart instanceof ITextEditor) { // TODO definition might start on ofset 0, if not linenumber
					ITextEditor textEditor = (ITextEditor) editorPart;
					IEditorInput input = editorPart.getEditorInput();
					IDocumentProvider provider = textEditor.getDocumentProvider();
					provider.connect(input);
					IDocument document = provider.getDocument(input);

					IRegion region = isOffset ? document.getLineInformationOfOffset(lineNumberOrOffset) : document.getLineInformation(lineNumberOrOffset - 1);
					textEditor.selectAndReveal(region.getOffset(), region.getLength());
					provider.disconnect(input);
				}
			} catch (PartInitException e) {
				TestFrameworkPlugin.log(e);
			} catch (BadLocationException e) {
				// unable to link
				TestFrameworkPlugin.log(e);
			} catch (CoreException e) {
				// unable to link
				TestFrameworkPlugin.log(e);
				return;
			}
		}
	}

	private IWorkbenchPage getActivePage() {
		IWorkbenchWindow window = TestFrameworkPlugin.getActiveWorkbenchWindow();
		if (window != null) {
			return window.getActivePage();
		}
		return null;
	}

	private String getEditorId(IFile file) {
		IWorkbench workbench = TestFrameworkPlugin.getDefault().getWorkbench();
		// If there is a registered editor for the file use it.
		IEditorDescriptor desc = workbench.getEditorRegistry().getDefaultEditor(file.getName(), getFileContentType(file));
		if (desc == null) {
			// default editor
			desc = workbench.getEditorRegistry().findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
		}
		return desc.getId();
	}

	private IContentType getFileContentType(IFile file) {
		try {
			IContentDescription description = file.getContentDescription();
			if (description != null) {
				return description.getContentType();
			}
		} catch (CoreException e) {
		}
		return null;
	}

}
