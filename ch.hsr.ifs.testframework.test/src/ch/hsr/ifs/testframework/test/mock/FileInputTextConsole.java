/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.test.mock;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.console.IConsoleDocumentPartitioner;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.part.IPageBookViewPage;
import org.osgi.framework.Bundle;

import ch.hsr.ifs.testframework.test.TestframeworkTestPlugin;

/**
 * @author Emanuel Graf
 * 
 */
public class FileInputTextConsole extends TextConsole {

	private final String inputFile;

	public FileInputTextConsole(String inputFile) {
		super(inputFile, "FileInputTextConsole", null, true);
		this.inputFile = inputFile;
	}

	private String getFileText(String inputFile) throws CoreException, IOException {
		Bundle bundle = TestframeworkTestPlugin.getDefault().getBundle();
		Path path = new Path(inputFile);
		BufferedReader br = null;
		try {
			String file2 = FileLocator.toFileURL(FileLocator.find(bundle, path, null)).getFile();
			br = new BufferedReader(new FileReader(file2));
			StringBuffer buffer = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null) {
				buffer.append(line);
				buffer.append('\n');
			}

			return buffer.toString();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void startTest() throws IOException {
		IDocument doc = getDocument();
		try {
			doc.replace(0, doc.getLength(), getFileText(inputFile));
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public IPageBookViewPage createPage(IConsoleView view) {
		return null;
	}

	@Override
	protected IConsoleDocumentPartitioner getPartitioner() {
		return null;
	}

	public void end() {
		dispose();
	}
}
