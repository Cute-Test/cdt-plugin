package ch.hsr.ifs.cute.elevator.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementVisitor;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.TextEditGroup;

import ch.hsr.ifs.cute.elevator.ast.ConstructorChainConverter;
import ch.hsr.ifs.cute.elevator.ast.DeclaratorCollector;
import ch.hsr.ifs.cute.elevator.ast.DeclaratorConverter;
import ch.hsr.ifs.cute.elevator.ast.InitializerCollector;

@SuppressWarnings("restriction")
public class ElevateProjectRefactoring extends CRefactoring {

    public ElevateProjectRefactoring(ICElement element, ISelection selection, ICProject project) {
        super(element, selection, project);   
    }

    @Override
    protected RefactoringDescriptor getRefactoringDescriptor() {
        return null;
    }
   
    @Override
    protected void collectModifications(IProgressMonitor pm, final ModificationCollector collector) throws CoreException, OperationCanceledException {
        for (ITranslationUnit translationUnit : getAllTranslationUnits()) {
            IASTTranslationUnit ast = getAST(translationUnit, new NullProgressMonitor());
            collectElevatableDeclarators(collector, ast);
            collectElevatableInitializers(collector, ast);
        }
    }

    private void collectElevatableInitializers(final ModificationCollector collector, IASTTranslationUnit ast) throws CoreException {
        InitializerCollector initializerCollector = new InitializerCollector();
        ast.accept(initializerCollector);
        for (ICPPASTConstructorChainInitializer initializer : initializerCollector.getInitializers()) {
            ICPPASTConstructorChainInitializer newNode = new ConstructorChainConverter(initializer).convert();
            collectChange(collector, initializer, newNode);
        }
    }

    private void collectElevatableDeclarators(final ModificationCollector collector, IASTTranslationUnit ast) throws CoreException {
        DeclaratorCollector declaratorCollector = new DeclaratorCollector();
        ast.accept(declaratorCollector);
        for (IASTDeclarator declarator : declaratorCollector.getDeclarators()) {
            IASTDeclarator newNode = new DeclaratorConverter(declarator).convert();
            collectChange(collector, declarator, newNode);
        }
    }

    private void collectChange(final ModificationCollector collector, IASTNode candidate, IASTNode newNode) throws CoreException {
        ASTRewrite rewriter = collector.rewriterForTranslationUnit(candidate.getTranslationUnit());
        rewriter.replace(candidate, newNode, new TextEditGroup("Elevate"));
    }

    private List<ITranslationUnit> getAllTranslationUnits() throws CoreException {
        final List<ITranslationUnit> translationUnits = new ArrayList<ITranslationUnit>();
        project.accept(new ICElementVisitor() {

            @Override
            public boolean visit(ICElement element) throws CoreException {
                if (isProjectOrContainer(element)) {
                    return true;
                }
                if (isTranslationUnit(element)) {
                    translationUnits.add((ITranslationUnit) element);
                }
                return false;    
            }

            private boolean isTranslationUnit(ICElement element) {
                return element.getElementType() == ICElement.C_UNIT;
            }
        });
        return translationUnits;
    }
    
    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        return super.checkInitialConditions(pm);
    }
    
    private boolean isProjectOrContainer(ICElement element) {
        return element.getElementType() == ICElement.C_PROJECT || element.getElementType() == ICElement.C_CCONTAINER;
    }
}
