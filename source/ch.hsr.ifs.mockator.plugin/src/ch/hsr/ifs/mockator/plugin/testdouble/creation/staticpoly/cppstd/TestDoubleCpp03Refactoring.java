package ch.hsr.ifs.mockator.plugin.testdouble.creation.staticpoly.cppstd;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.AbstractCreateTestDoubleRefactoring;
import ch.hsr.ifs.mockator.plugin.testdouble.movetons.TestDoubleInNsInserter;
import ch.hsr.ifs.mockator.plugin.testdouble.movetons.TestDoubleUsingNsHandler;

@SuppressWarnings("restriction")
class TestDoubleCpp03Refactoring extends AbstractCreateTestDoubleRefactoring {

  public TestDoubleCpp03Refactoring(ICElement cElement, ITextSelection selection, ICProject cProject) {
    super(cElement, selection, cProject);
  }

  @Override
  protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
      throws CoreException, OperationCanceledException {
    IASTTranslationUnit ast = getAST(tu, pm);
    ASTRewrite rewriter = createRewriter(collector, ast);
    String newClassName = getSelectedName(ast).get().toString();
    ICPPASTCompositeTypeSpecifier newTestDoubleClass = createNewTestDoubleClass(newClassName);

    for (ICPPASTFunctionDefinition optTestFun : getSelectedTestFunction(ast)) {
      insertTestDoubleInNamespace(optTestFun, rewriter, newTestDoubleClass);
      insertUsingNamespaceStmt(optTestFun, rewriter, newTestDoubleClass);
    }
  }

  private static void insertUsingNamespaceStmt(ICPPASTFunctionDefinition testFunction,
      ASTRewrite rewriter, ICPPASTCompositeTypeSpecifier testDouble) {
    TestDoubleUsingNsHandler namespaceHandler = new TestDoubleUsingNsHandler(testDouble, rewriter);
    namespaceHandler.insertUsingNamespaceStmt(testFunction);
  }

  private static void insertTestDoubleInNamespace(ICPPASTFunctionDefinition testFunction,
      ASTRewrite rewriter, ICPPASTCompositeTypeSpecifier testDouble) {
    TestDoubleInNsInserter inserter = new TestDoubleInNsInserter(rewriter, CppStandard.Cpp03Std);
    inserter.insertTestDouble(nodeFactory.newSimpleDeclaration(testDouble), testDouble,
        testFunction);
  }

  private Maybe<ICPPASTFunctionDefinition> getSelectedTestFunction(IASTTranslationUnit ast) {
    for (IASTName optFunName : getSelectedName(ast))
      return maybe((ICPPASTFunctionDefinition) AstUtil.getAncestorOfType(optFunName,
          ICPPASTFunctionDefinition.class));
    return none();
  }
}
