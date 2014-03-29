package ch.hsr.ifs.mockator.plugin.testdouble.movetons;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.fakeobject.FakeObject;
import ch.hsr.ifs.mockator.plugin.mockobject.MockObject;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.NodeContainer;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.TestDouble;
import ch.hsr.ifs.mockator.plugin.testdouble.support.TestDoubleKindAnalyzer;

@SuppressWarnings("restriction")
public class TestDoubleInNsInserter {
  private static final ICPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();
  private final ASTRewrite rewriter;
  private final CppStandard cppStd;

  public TestDoubleInNsInserter(ASTRewrite rewriter, CppStandard cppStd) {
    this.rewriter = rewriter;
    this.cppStd = cppStd;
  }

  public void insertTestDouble(IASTSimpleDeclaration testDouble,
      ICPPASTCompositeTypeSpecifier testDoubleToMove, ICPPASTFunctionDefinition testFunction) {
    Maybe<String> cuteSuiteName = getCuteSuiteName(testFunction);

    if (cuteSuiteName.isNone()) {
      insertTestDoubleInNs(testFunction.getTranslationUnit(), testFunction, testDouble,
          testDoubleToMove);
    } else {
      insertTestDoubleInCuteSuiteNs(testFunction, cuteSuiteName.get(), testDouble, testDoubleToMove);
    }
  }

  private static Maybe<String> getCuteSuiteName(ICPPASTFunctionDefinition testFunction) {
    CuteSuiteFinder cuteSuiteFinder = new CuteSuiteFinder(testFunction);
    testFunction.getTranslationUnit().accept(cuteSuiteFinder);
    return cuteSuiteFinder.getCuteSuiteName();
  }

  private void insertTestDoubleInCuteSuiteNs(ICPPASTFunctionDefinition testFunction,
      String cuteSuiteName, IASTSimpleDeclaration testDouble,
      ICPPASTCompositeTypeSpecifier testDoubleToMove) {
    IASTTranslationUnit testFunTu = testFunction.getTranslationUnit();
    Maybe<ICPPASTNamespaceDefinition> cuteSuiteNs =
        findNamespaceDefinition(testFunTu, cuteSuiteName);

    if (cuteSuiteNs.isNone()) {
      ICPPASTNamespaceDefinition newCuteNs = createNsWithName(cuteSuiteName);
      ICPPASTNamespaceDefinition funNs = createNsWithName(getNamespaceName(testFunction));
      newCuteNs.addDeclaration(funNs);
      addTestDoubleToNs(funNs, testDouble, testDoubleToMove);
      rewriter.insertBefore(testFunTu, testFunction, newCuteNs, null);
    } else {
      insertTestDoubleInNs(cuteSuiteNs.get(), testFunction, testDouble, testDoubleToMove);
    }
  }

  private void insertTestDoubleInNs(IASTNode parent, ICPPASTFunctionDefinition testFunction,
      IASTSimpleDeclaration testDouble, ICPPASTCompositeTypeSpecifier testDoubleToMove) {
    String funNameNs = getNamespaceName(testFunction);
    Maybe<ICPPASTNamespaceDefinition> testFunctionNs = findNamespaceDefinition(parent, funNameNs);

    if (testFunctionNs.isSome()) {
      ICPPASTNamespaceDefinition newTestFunctionNs = testFunctionNs.get().copy();
      addTestDoubleToNs(newTestFunctionNs, testDouble, testDoubleToMove);
      newTestFunctionNs.setParent(parent);
      rewriter.replace(testFunctionNs.get(), newTestFunctionNs, null);
    } else {
      ICPPASTNamespaceDefinition newTestFunctionNs = createNsWithName(funNameNs);
      addTestDoubleToNs(newTestFunctionNs, testDouble, testDoubleToMove);
      newTestFunctionNs.setParent(parent);
      ICPPASTFunctionDefinition insertionPoint =
          getInsertionPointForTestDouble(parent, testFunction);
      rewriter.insertBefore(parent, insertionPoint, newTestFunctionNs, null);
    }
  }

  private static ICPPASTFunctionDefinition getInsertionPointForTestDouble(IASTNode parent,
      ICPPASTFunctionDefinition testFunCandidate) {
    return parent instanceof IASTTranslationUnit ? testFunCandidate : null;
  }

  private void addTestDoubleToNs(ICPPASTNamespaceDefinition parentNs,
      IASTSimpleDeclaration simpleDecl, ICPPASTCompositeTypeSpecifier toMove) {
    TestDouble testDouble = null;

    switch (new TestDoubleKindAnalyzer(toMove).getKindOfTestDouble()) {
      case FakeObject:
        testDouble = new FakeObject(toMove);
        break;
      case MockObject:
        testDouble = new MockObject(toMove);
        break;
      default:
        throw new MockatorException("Unexpected kind of testdouble");
    }

    testDouble.addToNamespace(parentNs, simpleDecl, toMove, cppStd, rewriter);
  }

  private static String getNamespaceName(ICPPASTFunctionDefinition function) {
    return new NsNameGenerator().getNsNameFor(function);
  }

  private static ICPPASTNamespaceDefinition createNsWithName(String nsName) {
    return nodeFactory.newNamespaceDefinition(nodeFactory.newName(nsName.toCharArray()));
  }

  private static Maybe<ICPPASTNamespaceDefinition> findNamespaceDefinition(IASTNode parent,
      final String namespaceName) {
    final NodeContainer<ICPPASTNamespaceDefinition> namespace =
        new NodeContainer<ICPPASTNamespaceDefinition>();
    parent.accept(new ASTVisitor() {
      {
        shouldVisitNamespaces = true;
      }

      @Override
      public int visit(ICPPASTNamespaceDefinition namespaceDefinition) {
        if (namespaceName.equals(namespaceDefinition.getName().toString())) {
          namespace.setNode(namespaceDefinition);
          return PROCESS_ABORT;
        }

        return PROCESS_CONTINUE;
      }
    });

    return namespace.getNode();
  }
}
