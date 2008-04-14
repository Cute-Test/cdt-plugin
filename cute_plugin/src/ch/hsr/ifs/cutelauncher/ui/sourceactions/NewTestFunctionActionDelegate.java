/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule für Technik
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Emanuel Graf - initial API and implementation
 ******************************************************************************/
package ch.hsr.ifs.cutelauncher.ui.sourceactions;

import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.TextEdit;


/**
 * @author Emanuel Graf
 *
 */
public class NewTestFunctionActionDelegate extends AbstractFunctionActionDelegate {
	
	public NewTestFunctionActionDelegate(){
		super("newTestFunction",new NewTestFunctionAction());
	}
	@Override
	int getCursorEndPosition(TextEdit[] edits, String newLine) {
		for (TextEdit textEdit : edits) {
			String insert = ((InsertEdit)textEdit).getText();
			if(insert.contains(NewTestFunctionAction.TEST_STMT.trim())) {

				System.out.println(functionAction.insertFileOffset + " "+ functionAction.pushbackOffset);
				if(functionAction.insertFileOffset==-1 || functionAction.pushbackOffset==-1)//abnormal conditions
					return (textEdit.getOffset() + insert.indexOf(NewTestFunctionAction.TEST_STMT.trim()));
				if(functionAction.insertFileOffset < functionAction.pushbackOffset)
					return (textEdit.getOffset() + insert.indexOf(NewTestFunctionAction.TEST_STMT.trim()));
				else{
					return (textEdit.getOffset() + insert.indexOf(NewTestFunctionAction.TEST_STMT.trim())+functionAction.pushbackLength );
				}
			}
		}
		return edits[0].getOffset() + edits[0].getLength();
	}

	@Override
	int getExitPositionLength(){
		return NewTestFunctionAction.TEST_STMT.trim().length();
	}
}
