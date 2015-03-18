package ch.hsr.ifs.mockator.plugin.linker.wrapfun.gnuoption.qf;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.END_IF_DIRECTIVE;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
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

import ch.hsr.ifs.cdt.compatibility.changes.CCompositeChange;
import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.util.FileUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;

@SuppressWarnings("restriction")
class DeleteWrappedFunctionRefactoring extends MockatorRefactoring {
  private final IDocument doc;

  public DeleteWrappedFunctionRefactoring(ICElement element, ITextSelection selection,
      ICProject project, IDocument doc) {
    super(element, selection, project);
    this.doc = doc;
  }

  @Override
  public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
    CCompositeChange cChange = new CCompositeChange("Delete wrapped code segment");
    int offset = getSelection().getOffset();
    DeleteEdit deleteEdit = new DeleteEdit(offset, getLengthOfWrappedFunCode(offset));
    MultiTextEdit multiTextEdit = new MultiTextEdit();
    multiTextEdit.addChild(deleteEdit);
    TextFileChange change = createTextFileChange(pm, multiTextEdit);
    cChange.add(change);
    return cChange;
  }

  private TextFileChange createTextFileChange(IProgressMonitor pm, MultiTextEdit multiTextEdit)
      throws CoreException {
    IASTTranslationUnit ast = getAST(tu, pm);
    IFile tuFile = FileUtil.toIFile(ast.getFilePath());
    TextFileChange change = new TextFileChange("Delete wrapped function", tuFile);
    change.setEdit(multiTextEdit);
    return change;
  }

  private int getLengthOfWrappedFunCode(int offset) {
    try {
      String wrappedCode = doc.get(offset, doc.getLength() - offset);
      return wrappedCode.indexOf(END_IF_DIRECTIVE) + END_IF_DIRECTIVE.length();
    } catch (BadLocationException e) {
    }
    throw new MockatorException("Was not able to determine wrapped code segment");
  }

  @Override
  protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
      throws CoreException, OperationCanceledException {}

  @Override
  public String getDescription() {
    return I18N.DeleteWrappedFunctionRefactoringDesc;
  }
}
