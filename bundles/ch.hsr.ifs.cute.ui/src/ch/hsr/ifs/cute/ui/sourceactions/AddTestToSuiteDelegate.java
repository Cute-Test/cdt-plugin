/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.sourceactions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.RewriteSessionEditProcessor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

import ch.hsr.ifs.cute.core.CuteCorePlugin;


/**
 * @since 4.0
 */
public class AddTestToSuiteDelegate implements IEditorActionDelegate, IWorkbenchWindowActionDelegate {

   private IEditorPart                  editor;
   private final AbstractFunctionAction functionAction;

   public AddTestToSuiteDelegate() {
      functionAction = new AddTestToSuite();
   }

   @Override
   public void setActiveEditor(IAction action, IEditorPart targetEditor) {
      editor = targetEditor;
   }

   @Override
   public void dispose() {
      editor = null;
   }

   @Override
   public void init(IWorkbenchWindow window) {}

   @Override
   public void selectionChanged(IAction action, ISelection selection) {}

   @Override
   public void run(IAction action) {

      try {
         IEditorPart editor = getEditor();

         if (editor == null) return;

         TextEditor ceditor = (TextEditor) editor;
         IEditorInput editorInput = ceditor.getEditorInput();
         IDocumentProvider prov = ceditor.getDocumentProvider();
         IDocument doc = prov.getDocument(editorInput);

         MultiTextEdit mEdit;
         ISelection sel = ceditor.getSelectionProvider().getSelection();
         IFileEditorInput fei = editorInput.getAdapter(IFileEditorInput.class);
         if (fei != null) {
            mEdit = functionAction.createEdit(fei.getFile(), doc, sel);
            RewriteSessionEditProcessor processor = new RewriteSessionEditProcessor(doc, mEdit, TextEdit.CREATE_UNDO);
            processor.performEdits();
            this.editor = null;
         }
      } catch (CoreException e) {
         CuteCorePlugin.log(e);
      } catch (MalformedTreeException e) {
         CuteCorePlugin.log(e);
      } catch (BadLocationException e) {
         CuteCorePlugin.log(e);
      }
   }

   private IEditorPart getEditor() {
      if (editor == null) {
         IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
         if (page.isEditorAreaVisible() && page.getActiveEditor() != null && page.getActiveEditor() instanceof TextEditor) {
            editor = page.getActiveEditor();
         }
      }
      return editor;
   }
}
