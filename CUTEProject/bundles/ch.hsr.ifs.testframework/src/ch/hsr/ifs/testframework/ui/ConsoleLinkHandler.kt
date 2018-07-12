/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.TextConsole;

import ch.hsr.ifs.testframework.TestFrameworkPlugin;
import ch.hsr.ifs.testframework.event.TestEventHandler;
import kotlin.jvm.JvmOverloads


/**
 * @author Emanuel Graf (IFS)
 *
 */
class ConsoleLinkHandler @JvmOverloads constructor (val rtPath: IPath, val console: TextConsole, val linkFactory: ILinkFactory = ConsoleLinkFactory()) : TestEventHandler() {

   override fun handleBeginning(reg: IRegion, suitename: String, suitesize: String) = Unit

   override fun handleEnding(reg: IRegion, suitename: String) = Unit

   override fun handleError(reg: IRegion, testName: String, msg: String) = Unit

   override fun handleSuccess(reg: IRegion, name: String, msg: String) = Unit

   override fun handleFailure(reg: IRegion, testName: String, fileName: String, lineNo: String, reason: String) {
      val filePath = getWorkspaceFile(fileName, rtPath);
      try {
         val file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(filePath);
         val lineNumber = Integer.parseInt(lineNo);
         val link = linkFactory.createLink(file, lineNumber, null, -1, -1);
         console.addHyperlink(link, reg.getOffset(), reg.getLength());
      } catch (e: BadLocationException) {
         TestFrameworkPlugin.log(e);
      }
   }

   override fun handleTestStart(reg: IRegion, testName: String) = Unit

   override fun handleSessionStart() = Unit

   override fun handleSessionEnd() = Unit

}
