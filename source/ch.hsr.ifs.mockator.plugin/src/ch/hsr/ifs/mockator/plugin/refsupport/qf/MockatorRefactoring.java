package ch.hsr.ifs.mockator.plugin.refsupport.qf;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.array;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.last;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ui.ide.IDE;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.base.util.ProjectUtil;
import ch.hsr.ifs.mockator.plugin.base.util.UiUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.ClassInSelectionFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

@SuppressWarnings("restriction")
public abstract class MockatorRefactoring extends CRefactoring {
  protected static final ICPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();
  private final ITextSelection selection;

  public MockatorRefactoring(ICElement element, ITextSelection selection, ICProject project) {
    super(element, selection, project);
    this.selection = selection;
    saveAllDirtyEditors();
  }

  private void saveAllDirtyEditors() {
    UiUtil.runInDisplayThread(new F1V<RefactoringStatus>() {
      @Override
      public void apply(RefactoringStatus a) {
        if (!IDE.saveAllEditors(getWorkspaceRoot(), false)) {
          initStatus.addFatalError("Was not able to save all editors");
        }
      }
    }, initStatus);
  }

  private static IResource[] getWorkspaceRoot() {
    return new IResource[] {ProjectUtil.getWorkspaceRoot()};
  }

  protected ITextSelection getSelection() {
    return selection;
  }

  public abstract String getDescription();

  protected ASTRewrite createRewriter(ModificationCollector collector, IASTTranslationUnit ast) {
    return collector.rewriterForTranslationUnit(ast);
  }

  protected Maybe<ICPPASTCompositeTypeSpecifier> getClassInSelection(IASTTranslationUnit ast) {
    ClassInSelectionFinder finder = new ClassInSelectionFinder(getSelection(), ast);
    return finder.getClassInSelection();
  }

  protected Maybe<IASTName> checkSelectedNameIsInFunction(RefactoringStatus status,
      IProgressMonitor pm) throws CoreException {
    Maybe<IASTName> selectedName = getSelectedName(getAST(tu, pm));

    if (selectedName.isNone()) {
      status.addFatalError("Selection does not contain a name");
    }

    ICPPASTFunctionDefinition function = getParentFunction(selectedName.get());

    if (function == null) {
      status.addFatalError("Selection is not part of a function");
    }

    return selectedName;
  }

  protected ICPPASTFunctionDefinition getParentFunction(IASTName name) {
    return AstUtil.getAncestorOfType(name, ICPPASTFunctionDefinition.class);
  }

  @Override
  protected RefactoringStatus checkFinalConditions(IProgressMonitor subProgressMonitor,
      CheckConditionsContext checkContext) throws CoreException, OperationCanceledException {
    return initStatus;
  }

  @Override
  protected RefactoringDescriptor getRefactoringDescriptor() {
    return null;
  }

  protected Maybe<IASTName> getSelectedName(IASTTranslationUnit ast) {
    IASTNode selectedNode = getSelectedNode(ast);

    if (selectedNode instanceof IASTImplicitNameOwner
        && ((IASTImplicitNameOwner) selectedNode).getImplicitNames().length > 0)
      return findOperator(selectedNode);

    if (selectedNode instanceof IASTName)
      return maybe((IASTName) selectedNode);

    IASTName name = AstUtil.getAncestorOfType(selectedNode, IASTName.class);

    if (name != null)
      return maybe(name);

    return last(findAllMarkedNames(ast));
  }

  private static Maybe<IASTName> findOperator(IASTNode selectedNode) {
    for (IASTImplicitName iName : getNames(selectedNode)) {
      if (iName != null && iName.isOperator())
        return maybe((IASTName) iName);
    }

    return none();
  }

  private static IASTImplicitName[] getNames(IASTNode selectedNode) {
    if (selectedNode instanceof ICPPASTUnaryExpression
        || selectedNode instanceof ICPPASTBinaryExpression
        || selectedNode instanceof ICPPASTNewExpression
        || selectedNode instanceof ICPPASTDeleteExpression)
      return ((IASTImplicitNameOwner) selectedNode).getImplicitNames();

    return array();
  }

  protected IASTNode getSelectedNode(IASTTranslationUnit ast) {
    final String rootSourceOfTu = null;
    return ast.getNodeSelector(rootSourceOfTu).findEnclosingNodeInExpansion(selection.getOffset(),
        selection.getLength());
  }

  @Override
  protected IIndex getIndex() {
    try {
      return super.getIndex();
    } catch (OperationCanceledException e) {
      throw new MockatorException(e);
    } catch (CoreException e) {
      throw new MockatorException(e);
    }
  }
}
