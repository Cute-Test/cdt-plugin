package ch.hsr.ifs.mockator.plugin.testdouble.movetons;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.refsupport.utils.NodeContainer;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.QualifiedNameCreator;

@SuppressWarnings("restriction")
public class TestDoubleUsingNsHandler {
  private static final ICPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();
  private final ICPPASTCompositeTypeSpecifier testDouble;
  private final ASTRewrite rewriter;

  public TestDoubleUsingNsHandler(ICPPASTCompositeTypeSpecifier testDouble, ASTRewrite rewriter) {
    this.testDouble = testDouble;
    this.rewriter = rewriter;
  }

  public void insertUsingNamespaceStmt(ICPPASTFunctionDefinition testFunction) {
    ICPPASTQualifiedName qNameForUsing = getQNameForUsingNs();

    if (hasUsingNamespaceStmt(testFunction, qNameForUsing))
      return;

    IASTDeclarationStatement usingStmt = createUsingNameSpaceStmt(qNameForUsing);
    IASTNode firstPosInTestfun = getFirstPositionInFunBody(testFunction);
    rewriter.insertBefore(testFunction.getBody(), firstPosInTestfun, usingStmt, null);
  }

  private static IASTNode getFirstPositionInFunBody(ICPPASTFunctionDefinition testFunction) {
    IASTNode[] children = testFunction.getBody().getChildren();
    return children.length > 0 ? children[0] : null;
  }

  private ICPPASTQualifiedName getQNameForUsingNs() {
    ICPPASTQualifiedName qfNameForNs = nodeFactory.newQualifiedName();
    QualifiedNameCreator creator = new QualifiedNameCreator(testDouble.getName());
    IASTName[] names = creator.createQualifiedName().getNames();

    for (int i = 0; i < names.length - 1; ++i) {
      qfNameForNs.addName(names[i]);
    }

    return qfNameForNs;
  }

  private static boolean hasUsingNamespaceStmt(ICPPASTFunctionDefinition testFun,
      final ICPPASTQualifiedName qNameForUsing) {
    final NodeContainer<ICPPASTUsingDirective> usingDirective =
        new NodeContainer<ICPPASTUsingDirective>();
    testFun.accept(new ASTVisitor() {
      {
        shouldVisitDeclarations = true;
      }

      @Override
      public int visit(IASTDeclaration decl) {
        if (!(decl instanceof ICPPASTUsingDirective))
          return PROCESS_CONTINUE;

        ICPPASTUsingDirective using = (ICPPASTUsingDirective) decl;

        if (using.getQualifiedName().toString().equals(qNameForUsing.toString())) {
          usingDirective.setNode(using);
          return PROCESS_ABORT;
        }

        return PROCESS_CONTINUE;
      }
    });
    return usingDirective.getNode().isSome();
  }

  private static IASTDeclarationStatement createUsingNameSpaceStmt(
      ICPPASTQualifiedName qNameForUsing) {
    ICPPASTUsingDirective usingDirective = nodeFactory.newUsingDirective(qNameForUsing);
    return nodeFactory.newDeclarationStatement(usingDirective);
  }
}
