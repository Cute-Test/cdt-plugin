package ch.hsr.ifs.cdt.namespactor.refactoring.iu;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.internal.ui.refactoring.utils.SelectionHelper;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.cdt.namespactor.astutil.NSSelectionHelper;
import ch.hsr.ifs.cdt.namespactor.refactoring.TemplateIdFactory;
import ch.hsr.ifs.cdt.namespactor.refactoring.iudec.IUDECRefactoring;
import ch.hsr.ifs.cdt.namespactor.refactoring.iudir.IUDIRRefactoring;
import ch.hsr.ifs.cdt.namespactor.refactoring.rewrite.ASTRewriteStore;

@SuppressWarnings("restriction")
public class IURefactoring extends InlineRefactoringBase {
	private InlineRefactoringBase delegate=null;
	private ICElement element=null;
	private ISelection selection;
	private ICProject project;
	public IURefactoring(ICElement element, ISelection selection,
			ICProject project) {
		super(element, selection, project);
		this.element=element;
		this.selection=selection;
		this.project=project;
	}

	@Override
	protected void collectModifications(ASTRewriteStore store) {
		delegate.collectModifications(store);
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		// determine delegate based on selection
		Region region = getSelectedRegion();

		IASTTranslationUnit ast = this.refactoringContext.getAST(this.tu,pm);// warnings...
		if(NSSelectionHelper.getSelectedUsingDirective(region, ast) != null){
			delegate = new IUDIRRefactoring(element, selection, project);
			
		}else if(NSSelectionHelper.getSelectedUsingDeclaration(region, ast) != null){
			delegate = new IUDECRefactoring(element, selection, project);
		}else{
//			MessageDialog.openError(shellProvider.getShell(), "Inline Refactoring Startup Error", Labels.IU_NoUsingSelected);
		}
		delegate.setContext(this.refactoringContext);
		
		return delegate.checkInitialConditions(pm);
	}

	@Override
	protected TemplateIdFactory getTemplateIdFactory(
			ICPPASTTemplateId templateId, InlineRefactoringContext ctx) {
		return delegate.getTemplateIdFactory(templateId, ctx);
	}

	private Region getSelectedRegion() {
		Region region = null;
		if (selection instanceof ITextSelection) {
			region = SelectionHelper.getRegion(selection);
		} else {
			try {
				ISourceRange sourceRange = ((ISourceReference) element).getSourceRange();
				region = new Region(sourceRange.getIdStartPos(), sourceRange.getIdLength());
			} catch (CModelException e) {
				CUIPlugin.log(e);
			}
		}
		return region;
	}


}
