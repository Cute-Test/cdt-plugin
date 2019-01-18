/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.ui

import org.eclipse.cdt.core.CCorePlugin
import org.eclipse.cdt.core.dom.ast.IASTFileLocation
import org.eclipse.cdt.core.index.IIndex
import org.eclipse.cdt.core.index.IIndexBinding
import org.eclipse.cdt.core.index.IIndexName
import org.eclipse.cdt.core.index.IndexFilter
import org.eclipse.cdt.core.model.CModelException
import org.eclipse.cdt.core.model.CoreModel
import org.eclipse.cdt.core.model.ICProject
import org.eclipse.cdt.core.parser.util.ArrayUtil
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.content.IContentDescription
import org.eclipse.core.runtime.content.IContentType
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.jface.text.BadLocationException
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.IRegion
import org.eclipse.jface.viewers.DoubleClickEvent
import org.eclipse.jface.viewers.IDoubleClickListener
import org.eclipse.jface.viewers.TreeSelection
import org.eclipse.ui.IEditorDescriptor
import org.eclipse.ui.IEditorInput
import org.eclipse.ui.IEditorPart
import org.eclipse.ui.IEditorRegistry
import org.eclipse.ui.IWorkbench
import org.eclipse.ui.IWorkbenchPage
import org.eclipse.ui.IWorkbenchWindow
import org.eclipse.ui.PartInitException
import org.eclipse.ui.part.FileEditorInput
import org.eclipse.ui.texteditor.IDocumentProvider
import org.eclipse.ui.texteditor.ITextEditor

import ch.hsr.ifs.testframework.TestFrameworkPlugin
import ch.hsr.ifs.testframework.model.TestCase
import ch.hsr.ifs.testframework.model.TestSession
import ch.hsr.ifs.testframework.model.TestStatus


private val REGEX   = Regex("::")

/**
 * @author Emanuel Graf
 *
 */
class CuteTestDClickListener(private var session: TestSession?) : IDoubleClickListener {


   override fun doubleClick(event: DoubleClickEvent) =
         with(event.selection) {
            if(this is TreeSelection && this.firstElement is TestCase) {
               val case = this.firstElement as TestCase
               when(case.getStatus()) {
                  TestStatus.failure -> openEditor(case.getFile()!!, case.getLineNumber(), false)
                  else -> openEditorForNonFailingTestCase(case.getName())
               }
            }
         }

   fun setSession(session: TestSession) {
      this.session = session
   }

   fun openEditorForNonFailingTestCase(testCaseName: String) {
      try {
         val launchConfiguration = session?.launch!!.getLaunchConfiguration()
         val launchConfigName = launchConfiguration.getAttribute("org.eclipse.cdt.launch.PROJECT_ATTR", launchConfiguration.getName())
         CoreModel.getDefault().getCModel().getCProjects().forEach{
            val projectName = it.getElementName()
            if (!projectName.equals(launchConfigName)) return
            val index = CCorePlugin.getIndexManager().getIndex(it)
            val bindings = getBindings(testCaseName, index)
            checkBindingsOpenEditor(index, bindings)
         }
      } catch (e: CModelException) {
         TestFrameworkPlugin.log(e)
      } catch (e: CoreException) {
         TestFrameworkPlugin.log(e)
      }
   }

   private fun checkBindingsOpenEditor(index: IIndex, bindings: Array<IIndexBinding?>?) {
      bindings?.forEach{
         if (it == null) return

         val definition = index.findDefinitions(index.adaptBinding(it))
         if (definition == null || definition.size == 0) return
         val loc = definition[0].getFileLocation()
         val filePath = Path(loc.getFileName())
         val file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(filePath)
         if (file == null) return
         openEditor(file, loc.getNodeOffset(), true)
         return
      }
   }

   /**
    * @since 3.0
    */
   private fun getBindings(testCaseName: String, index: IIndex) =
      if (isQualifiedName(testCaseName)) {
         index.findBindings(getNamesArray(testCaseName), IndexFilter.ALL, NullProgressMonitor())
      } else {
         index.findBindings(testCaseName.toCharArray(), false, IndexFilter.ALL, NullProgressMonitor())
      }

   /**
    * @since 3.0
    */
   private fun getNamesArray(testCaseName: String) =
      removeEmptyNames(testCaseName.split(REGEX)).map(String::toCharArray).toTypedArray()

   private fun removeEmptyNames(split: List<String>) = split.filter { !it.isEmpty() }.toList()

   private fun isQualifiedName(testCaseName: String) = testCaseName.contains(REGEX)

   private fun openEditor(file: IFile, lineNumberOrOffset: Int, isOffset: Boolean) =
      TestFrameworkPlugin.activePage?.apply{
         try {
            val editor = openEditor(FileEditorInput(file), getEditorId(file), false)
            if(lineNumberOrOffset > 0 && editor is ITextEditor) {
               val input = editor.editorInput
               val provider = editor.documentProvider
               provider.connect(input)
               val document = provider.getDocument(input)
               val region = if(isOffset) document.getLineInformationOfOffset(lineNumberOrOffset) else document.getLineInformation(lineNumberOrOffset - 1)
               editor.selectAndReveal(region.offset, region.length)
               provider.disconnect(input)
            }
         } catch (e: Throwable) {
            TestFrameworkPlugin.log(e)
         }
      }

   private fun getEditorId(file: IFile) =
         with(TestFrameworkPlugin.default.workbench, {
            getEditorRegistry()
               .getDefaultEditor(file.getName(), getFileContentType(file))
               ?.getId()
               ?:getEditorRegistry()
               .findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID).getId()
         })

   private fun getFileContentType(file: IFile): IContentType? {
      try {
         val description = file.getContentDescription()
         if (description != null) { return description.getContentType() }
      } catch (e: CoreException) {}
      return null
   }

}
