package ch.hsr.ifs.mockator.plugin.mockobject;

import static ch.hsr.ifs.iltis.core.collections.CollectionUtil.head;
import static ch.hsr.ifs.iltis.core.collections.CollectionUtil.list;
import static ch.hsr.ifs.mockator.plugin.MockatorConstants.MOCK_ID;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.iltis.cpp.wrappers.CPPVisitor;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.incompleteclass.DefaultCtorProvider;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFunImplStrategy;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.ExpectedNameCreator;
import ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder.MockCallRegistrationFinder;
import ch.hsr.ifs.mockator.plugin.mockobject.support.allcalls.AllCallsVectorFinderVisitor;
import ch.hsr.ifs.mockator.plugin.mockobject.support.allcalls.AllCallsVectorNameCreator;
import ch.hsr.ifs.mockator.plugin.mockobject.support.memfun.CtorMockSupport;
import ch.hsr.ifs.mockator.plugin.mockobject.support.memfun.MemFunMockSupport;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.testdouble.MemFunMockSupportAdder;
import ch.hsr.ifs.mockator.plugin.testdouble.PolymorphismKind;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.AbstractTestDouble;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.ExistingTestDoubleMemFun;


public class MockObject extends AbstractTestDouble {

   public MockObject(final ICPPASTCompositeTypeSpecifier clazz) {
      super(clazz);
   }

   public Collection<ExistingTestDoubleMemFun> getRegisteredMemFuns(final CppStandard cppStd) {
      return getPublicMemFuns().stream().filter((function) -> function.getRegisteredCall(new MockCallRegistrationFinder(cppStd)).isPresent()).collect(
            Collectors.toList());
   }

   @Override
   public void addToNamespace(final ICPPASTNamespaceDefinition parentNs, final IASTSimpleDeclaration testDouble,
         final ICPPASTCompositeTypeSpecifier testDoubleToMove, final CppStandard cppStd, final ASTRewrite rewriter) {
      final MockObjectToNsAdder nsAdder = new MockObjectToNsAdder(cppStd, testDoubleToMove);
      nsAdder.addTestDoubleToNs(testDouble, parentNs);
      removeAllCallsVector(rewriter);
   }

   private void removeAllCallsVector(final ASTRewrite rewriter) {
      getAllCallsVector().ifPresent((calls) -> rewriter.remove(CPPVisitor.findAncestorWithType(calls, IASTDeclarationStatement.class).orElse(null),
            null));
   }

   @Override
   public void addAdditionalCtorSupport(final ICPPASTFunctionDefinition defaultCtor, final CppStandard cppStd) {
      new DefaultCtorMockSupportAdder(cppStd, getPolymorphismKind(), getNameOfAllCallsVector()).accept(defaultCtor);
   }

   @Override
   public DefaultCtorProvider getDefaultCtorProvider(final CppStandard cppStd) {
      return new MockObjectDefaultCtorProvider(getKlass(), cppStd);
   }

   @Override
   protected TestDoubleMemFunImplStrategy getImplStrategy(final CppStandard cppStd) {
      return new MockObjectMemFunImplStrategy(cppStd, this);
   }

   public boolean hasMockIdField() {
      final Collection<IASTDeclaration> mockIdField = Arrays.asList(getKlass().getMembers()).stream().filter((decl) -> isMockIdField(decl)).collect(
            Collectors.toList());
      return !mockIdField.isEmpty();
   }

   private static boolean isMockIdField(final IASTDeclaration declaration) {
      if (!(declaration instanceof IASTSimpleDeclaration)) { return false; }

      final IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) declaration;
      return hasMockIdName(simpleDecl) && hasMockIdType(simpleDecl);
   }

   private static boolean hasMockIdType(final IASTSimpleDeclaration simpleDecl) {
      final IASTDeclSpecifier declSpecifier = simpleDecl.getDeclSpecifier();

      if (!(declSpecifier instanceof ICPPASTNamedTypeSpecifier)) { return false; }

      return isConstSizeT((ICPPASTNamedTypeSpecifier) declSpecifier);
   }

   private static boolean isConstSizeT(final ICPPASTNamedTypeSpecifier namedSpec) {
      return namedSpec.getName().toString().equals(MockatorConstants.SIZE_T) && namedSpec.isConst();
   }

   private static boolean hasMockIdName(final IASTSimpleDeclaration simpleDecl) {
      final IASTDeclarator[] declarators = simpleDecl.getDeclarators();

      if (declarators.length != 1) { return false; }

      return declarators[0].getName().toString().equals(MOCK_ID);
   }

   public String getNameOfAllCallsVector() {
      final AllCallsVectorNameCreator creator = new AllCallsVectorNameCreator(getKlass(), getParent());
      return creator.getNameOfAllCallsVector();
   }

   public Optional<IASTName> getRegistrationVector() {
      final AllCallsVectorFinderVisitor finder = new AllCallsVectorFinderVisitor();
      getKlass().accept(finder);
      return finder.getFoundCallsVector();
   }

   public Optional<IASTName> getAllCallsVector() {
      return getRegistrationVector().flatMap(vector -> head(list(getKlass().getTranslationUnit().getDefinitionsInAST(vector.resolveBinding()))));
   }

   public String getNameForExpectationVector() {
      return new ExpectedNameCreator(getName()).getNameForExpectationsVector();
   }

   public String getFqNameOfAllCallsVector() {
      final AllCallsVectorNameCreator creator = new AllCallsVectorNameCreator(getKlass(), getParent());
      return creator.getFqNameOfAllCallsVector();
   }

   public MemFunMockSupportAdder getMockSupport(final ASTRewrite rewriter, final CppStandard std, final ExistingTestDoubleMemFun memFun) {
      final String callsVectorName = getNameOfAllCallsVector();
      final PolymorphismKind polyKind = getPolymorphismKind();

      if (memFun.isConstructor()) {
         return new CtorMockSupport(rewriter, std, callsVectorName, polyKind);
      } else {
         return new MemFunMockSupport(rewriter, std, callsVectorName, polyKind);
      }
   }
}
