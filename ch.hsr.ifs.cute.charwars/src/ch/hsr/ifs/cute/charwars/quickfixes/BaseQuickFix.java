package ch.hsr.ifs.cute.charwars.quickfixes;

import java.util.HashSet;

import org.eclipse.cdt.codan.ui.AbstractAstRewriteQuickFix;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.asttools.ASTModifier;
import ch.hsr.ifs.cute.charwars.asttools.ASTRewriteCache;
import ch.hsr.ifs.cute.charwars.constants.ErrorMessages;
import ch.hsr.ifs.cute.charwars.dialogs.ErrorRefactoring;
import ch.hsr.ifs.cute.charwars.dialogs.ErrorRefactoringWizard;
import ch.hsr.ifs.cute.charwars.loggers.ErrorLogger;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class BaseQuickFix extends AbstractAstRewriteQuickFix {
	protected HashSet<String> headers = new HashSet<String>();
	
	@SuppressFBWarnings(value="URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
	protected IMarker currentMarker = null;
	
	@Override
	public boolean isApplicable(IMarker marker) {
		currentMarker = marker;
		return super.isApplicable(marker);
	}
	
	@Override
	public void modifyAST(IIndex index, IMarker marker) {
		try {
			currentMarker = marker;
			ASTRewriteCache rewriteCache = new ASTRewriteCache(index);
			IASTTranslationUnit astTranslationUnit = rewriteCache.getASTTranslationUnit(getTranslationUnitViaEditor(marker));
			IASTNode markedNode = getMarkedNode(astTranslationUnit, marker);
			if(markedNode instanceof IASTName) {
				markedNode = markedNode.getParent();
			}
			handleMarkedNode(markedNode, rewriteCache);
			performChange(rewriteCache.getChange(), marker);
			ASTModifier.includeHeaders(headers, astTranslationUnit, getDocument());
		} 
		catch(Exception e) {
			e.printStackTrace();
			ErrorRefactoring refactoring = new ErrorRefactoring(getErrorMessage());
			ErrorRefactoringWizard refactoringWizard = new ErrorRefactoringWizard(refactoring, 0);
			RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(refactoringWizard);
			
			try {
				op.run(null, ErrorMessages.ALERT_BOX_TITLE);
			} 
			catch(InterruptedException e1) {
				ErrorLogger.log(ErrorMessages.UNABLE_TO_SHOW_ALERT_BOX, e1);
			}
		}
	}
	
	private IASTNode getMarkedNode(IASTTranslationUnit astTranslationUnit, IMarker marker) {
		int start = marker.getAttribute(IMarker.CHAR_START, -1);
		int end = marker.getAttribute(IMarker.CHAR_END, -1);
		return ASTAnalyzer.getMarkedNode(astTranslationUnit, start, end - start);
	}
	
	protected abstract void handleMarkedNode(IASTNode markedNode, ASTRewriteCache rewriteCache);
	protected abstract String getErrorMessage();
	
	private void performChange(Change change, IMarker marker) {
		try {
			change.perform(new NullProgressMonitor());
			marker.delete();
		}
		catch(CoreException e) {
			ErrorLogger.log(ErrorMessages.UNABLE_TO_DELETE_MARKER, e);
		}
	}
}
