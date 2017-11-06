package ch.hsr.ifs.mockator.plugin.linker.wrapfun.gnuoption.qf;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.END_IF_DIRECTIVE;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.changes.CCompositeChange;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.util.FileUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;


@SuppressWarnings("restriction")
class DeleteWrappedFunctionRefactoring extends MockatorRefactoring {

   private final IDocument doc;

   public DeleteWrappedFunctionRefactoring(final ICElement element, final ITextSelection selection, final ICProject project, final IDocument doc) {
      super(element, selection, project);
      this.doc = doc;
   }

   @Override
   public Change createChange(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
      final CCompositeChange cChange = new CCompositeChange("Delete wrapped code segment");
      final int offset = getSelection().getOffset();
      final DeleteEdit deleteEdit = new DeleteEdit(offset, getLengthOfWrappedFunCode(offset));
      final MultiTextEdit multiTextEdit = new MultiTextEdit();
      multiTextEdit.addChild(deleteEdit);
      final TextFileChange change = createTextFileChange(pm, multiTextEdit);
      cChange.add(change);
      return cChange;
   }

   private TextFileChange createTextFileChange(final IProgressMonitor pm, final MultiTextEdit multiTextEdit) throws CoreException {
      final IASTTranslationUnit ast = getAST(tu, pm);
      final IFile tuFile = FileUtil.toIFile(ast.getFilePath());
      final TextFileChange change = new TextFileChange("Delete wrapped function", tuFile);
      change.setEdit(multiTextEdit);
      return change;
   }

   private int getLengthOfWrappedFunCode(final int offset) {
      try {
         final String wrappedCode = doc.get(offset, doc.getLength() - offset);
         return wrappedCode.indexOf(END_IF_DIRECTIVE) + END_IF_DIRECTIVE.length();
      }
      catch (final BadLocationException e) {}
      throw new MockatorException("Was not able to determine wrapped code segment");
   }

   @Override
   protected void collectModifications(final IProgressMonitor pm, final ModificationCollector collector) throws CoreException,
         OperationCanceledException {}

   @Override
   public String getDescription() {
      return I18N.DeleteWrappedFunctionRefactoringDesc;
   }
}
