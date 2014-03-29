package ch.hsr.ifs.mockator.plugin.mockobject.support.allcalls;

import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.testdouble.TestDoubleParentFinder;

// Inserts the following code before the mock object:
// static std::vector<calls> allCalls(1);
// Note that the vector is allocated with one element reserved for static function calls.
@SuppressWarnings("restriction")
public class AllCallsVectorInserter {
  private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();
  private final ICPPASTCompositeTypeSpecifier mockObject;
  private final CppStandard cppStd;
  private final Maybe<IASTName> allCallsVector;
  private final IASTNode parent;

  public AllCallsVectorInserter(ICPPASTCompositeTypeSpecifier mockObject, IASTNode parent,
      Maybe<IASTName> allCallsVector, CppStandard cppStd) {
    this.mockObject = mockObject;
    this.parent = parent;
    this.allCallsVector = allCallsVector;
    this.cppStd = cppStd;
  }

  public void insert(ASTRewrite rewriter) {
    if (allCallsVector.isNone()) {
      IASTNode parent = new TestDoubleParentFinder(mockObject).getParentOfTestDouble();
      AllCallsVectorCreator creator =
          new AllCallsVectorCreator(getNameOfAllCallsVector(), parent, cppStd);
      IASTSimpleDeclaration allCallsVector = creator.getAllCallsVector();
      insertAllCallsVector(getAllCallsVector(allCallsVector, parent), parent, rewriter);
    }
  }

  private String getNameOfAllCallsVector() {
    AllCallsVectorNameCreator creator = new AllCallsVectorNameCreator(mockObject, parent);
    return creator.getNameOfAllCallsVector();
  }

  private static IASTNode getAllCallsVector(IASTSimpleDeclaration allCallsVector, IASTNode parent) {
    if (isPartOfFunction(parent))
      return nodeFactory.newDeclarationStatement(allCallsVector);

    return allCallsVector;
  }

  private void insertAllCallsVector(IASTNode partToInsert, IASTNode parent, ASTRewrite rewrite) {
    final IASTNode insertionPoint = getInsertionPoint(parent);
    rewrite.insertBefore(parent, insertionPoint, partToInsert, null);
  }

  private IASTNode getInsertionPoint(IASTNode parent) {
    if (isPartOfFunction(parent))
      // right before test double
      return AstUtil.getAncestorOfType(mockObject, IASTDeclarationStatement.class);
    else
      return AstUtil.getAncestorOfType(mockObject, IASTSimpleDeclaration.class);
  }

  private static boolean isPartOfFunction(IASTNode node) {
    return AstUtil.<ICPPASTFunctionDefinition>getAncestorOfType(node,
        ICPPASTFunctionDefinition.class) != null;
  }
}
