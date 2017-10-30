package ch.hsr.ifs.mockator.plugin.testdouble.movetons;

import java.util.Optional;

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
import ch.hsr.ifs.mockator.plugin.fakeobject.FakeObject;
import ch.hsr.ifs.mockator.plugin.mockobject.MockObject;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.NodeContainer;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.TestDouble;
import ch.hsr.ifs.mockator.plugin.testdouble.support.TestDoubleKindAnalyzer;


@SuppressWarnings("restriction")
public class TestDoubleInNsInserter {

   private static final ICPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();
   private final ASTRewrite             rewriter;
   private final CppStandard            cppStd;

   public TestDoubleInNsInserter(final ASTRewrite rewriter, final CppStandard cppStd) {
      this.rewriter = rewriter;
      this.cppStd = cppStd;
   }

   public void insertTestDouble(final IASTSimpleDeclaration testDouble, final ICPPASTCompositeTypeSpecifier testDoubleToMove,
            final ICPPASTFunctionDefinition testFunction) {
      final Optional<String> cuteSuiteName = getCuteSuiteName(testFunction);

      if (!cuteSuiteName.isPresent()) {
         insertTestDoubleInNs(testFunction.getTranslationUnit(), testFunction, testDouble, testDoubleToMove);
      } else {
         insertTestDoubleInCuteSuiteNs(testFunction, cuteSuiteName.get(), testDouble, testDoubleToMove);
      }
   }

   private static Optional<String> getCuteSuiteName(final ICPPASTFunctionDefinition testFunction) {
      final CuteSuiteFinder cuteSuiteFinder = new CuteSuiteFinder(testFunction);
      testFunction.getTranslationUnit().accept(cuteSuiteFinder);
      return cuteSuiteFinder.getCuteSuiteName();
   }

   private void insertTestDoubleInCuteSuiteNs(final ICPPASTFunctionDefinition testFunction, final String cuteSuiteName,
            final IASTSimpleDeclaration testDouble, final ICPPASTCompositeTypeSpecifier testDoubleToMove) {
      final IASTTranslationUnit testFunTu = testFunction.getTranslationUnit();
      final Optional<ICPPASTNamespaceDefinition> cuteSuiteNs = findNamespaceDefinition(testFunTu, cuteSuiteName);

      if (!cuteSuiteNs.isPresent()) {
         final ICPPASTNamespaceDefinition newCuteNs = createNsWithName(cuteSuiteName);
         final ICPPASTNamespaceDefinition funNs = createNsWithName(getNamespaceName(testFunction));
         newCuteNs.addDeclaration(funNs);
         addTestDoubleToNs(funNs, testDouble, testDoubleToMove);
         rewriter.insertBefore(testFunTu, testFunction, newCuteNs, null);
      } else {
         insertTestDoubleInNs(cuteSuiteNs.get(), testFunction, testDouble, testDoubleToMove);
      }
   }

   private void insertTestDoubleInNs(final IASTNode parent, final ICPPASTFunctionDefinition testFunction, final IASTSimpleDeclaration testDouble,
            final ICPPASTCompositeTypeSpecifier testDoubleToMove) {
      final String funNameNs = getNamespaceName(testFunction);
      final Optional<ICPPASTNamespaceDefinition> testFunctionNs = findNamespaceDefinition(parent, funNameNs);

      if (testFunctionNs.isPresent()) {
         final ICPPASTNamespaceDefinition newTestFunctionNs = testFunctionNs.get().copy();
         addTestDoubleToNs(newTestFunctionNs, testDouble, testDoubleToMove);
         newTestFunctionNs.setParent(parent);
         rewriter.replace(testFunctionNs.get(), newTestFunctionNs, null);
      } else {
         final ICPPASTNamespaceDefinition newTestFunctionNs = createNsWithName(funNameNs);
         addTestDoubleToNs(newTestFunctionNs, testDouble, testDoubleToMove);
         newTestFunctionNs.setParent(parent);
         final ICPPASTFunctionDefinition insertionPoint = getInsertionPointForTestDouble(parent, testFunction);
         rewriter.insertBefore(parent, insertionPoint, newTestFunctionNs, null);
      }
   }

   private static ICPPASTFunctionDefinition getInsertionPointForTestDouble(final IASTNode parent, final ICPPASTFunctionDefinition testFunCandidate) {
      return parent instanceof IASTTranslationUnit ? testFunCandidate : null;
   }

   private void addTestDoubleToNs(final ICPPASTNamespaceDefinition parentNs, final IASTSimpleDeclaration simpleDecl,
            final ICPPASTCompositeTypeSpecifier toMove) {
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

   private static String getNamespaceName(final ICPPASTFunctionDefinition function) {
      return new NsNameGenerator().getNsNameFor(function);
   }

   private static ICPPASTNamespaceDefinition createNsWithName(final String nsName) {
      return nodeFactory.newNamespaceDefinition(nodeFactory.newName(nsName.toCharArray()));
   }

   private static Optional<ICPPASTNamespaceDefinition> findNamespaceDefinition(final IASTNode parent, final String namespaceName) {
      final NodeContainer<ICPPASTNamespaceDefinition> namespace = new NodeContainer<>();
      parent.accept(new ASTVisitor() {

         {
            shouldVisitNamespaces = true;
         }

         @Override
         public int visit(final ICPPASTNamespaceDefinition namespaceDefinition) {
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
