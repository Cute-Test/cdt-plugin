package ch.hsr.ifs.mockator.plugin.mockobject;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.MOCK_ID;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.head;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder.filter;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.functional.F1;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.incompleteclass.DefaultCtorProvider;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFunImplStrategy;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.ExpectedNameCreator;
import ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder.MockCallRegistrationFinder;
import ch.hsr.ifs.mockator.plugin.mockobject.support.allcalls.AllCallsVectorFinderVisitor;
import ch.hsr.ifs.mockator.plugin.mockobject.support.allcalls.AllCallsVectorNameCreator;
import ch.hsr.ifs.mockator.plugin.mockobject.support.memfun.CtorMockSupport;
import ch.hsr.ifs.mockator.plugin.mockobject.support.memfun.MemFunMockSupport;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.testdouble.MemFunMockSupportAdder;
import ch.hsr.ifs.mockator.plugin.testdouble.PolymorphismKind;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.AbstractTestDouble;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.ExistingTestDoubleMemFun;

public class MockObject extends AbstractTestDouble {

  public MockObject(ICPPASTCompositeTypeSpecifier klass) {
    super(klass);
  }

  public Collection<ExistingTestDoubleMemFun> getRegisteredMemFuns(final CppStandard cppStd) {
    return filter(getPublicMemFuns(), new F1<ExistingTestDoubleMemFun, Boolean>() {
      @Override
      public Boolean apply(ExistingTestDoubleMemFun function) {
        MockCallRegistrationFinder finder = new MockCallRegistrationFinder(cppStd);
        return function.getRegisteredCall(finder).isSome();
      }
    });
  }

  @Override
  public void addToNamespace(ICPPASTNamespaceDefinition parentNs, IASTSimpleDeclaration testDouble,
      ICPPASTCompositeTypeSpecifier testDoubleToMove, CppStandard cppStd, ASTRewrite rewriter) {
    MockObjectToNsAdder nsAdder = new MockObjectToNsAdder(cppStd, testDoubleToMove);
    nsAdder.addTestDoubleToNs(testDouble, parentNs);
    removeAllCallsVector(rewriter);
  }

  private void removeAllCallsVector(ASTRewrite rewriter) {
    for (IASTNode optCalls : getAllCallsVector()) {
      IASTDeclarationStatement callsVector =
          AstUtil.getAncestorOfType(optCalls, IASTDeclarationStatement.class);
      rewriter.remove(callsVector, null);
    }
  }

  @Override
  public void addAdditionalCtorSupport(ICPPASTFunctionDefinition defaultCtor, CppStandard cppStd) {
    new DefaultCtorMockSupportAdder(cppStd, getPolymorphismKind(), getNameOfAllCallsVector())
        .apply(defaultCtor);
  }

  @Override
  public DefaultCtorProvider getDefaultCtorProvider(CppStandard cppStd) {
    return new MockObjectDefaultCtorProvider(getKlass(), cppStd);
  }

  @Override
  protected TestDoubleMemFunImplStrategy getImplStrategy(CppStandard cppStd) {
    return new MockObjectMemFunImplStrategy(cppStd, this);
  }

  public boolean hasMockIdField() {
    Collection<IASTDeclaration> mockIdField =
        filter(getKlass().getMembers(), new F1<IASTDeclaration, Boolean>() {
          @Override
          public Boolean apply(IASTDeclaration decl) {
            return isMockIdField(decl);
          }

        });
    return !mockIdField.isEmpty();
  }

  private static boolean isMockIdField(IASTDeclaration declaration) {
    if (!(declaration instanceof IASTSimpleDeclaration))
      return false;

    IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) declaration;
    return hasMockIdName(simpleDecl) && hasMockIdType(simpleDecl);
  }

  private static boolean hasMockIdType(IASTSimpleDeclaration simpleDecl) {
    IASTDeclSpecifier declSpecifier = simpleDecl.getDeclSpecifier();

    if (!(declSpecifier instanceof ICPPASTNamedTypeSpecifier))
      return false;

    return isConstSizeT((ICPPASTNamedTypeSpecifier) declSpecifier);
  }

  private static boolean isConstSizeT(ICPPASTNamedTypeSpecifier namedSpec) {
    return namedSpec.getName().toString().equals(MockatorConstants.SIZE_T) && namedSpec.isConst();
  }

  private static boolean hasMockIdName(IASTSimpleDeclaration simpleDecl) {
    IASTDeclarator[] declarators = simpleDecl.getDeclarators();

    if (declarators.length != 1)
      return false;

    return declarators[0].getName().toString().equals(MOCK_ID);
  }

  public String getNameOfAllCallsVector() {
    AllCallsVectorNameCreator creator = new AllCallsVectorNameCreator(getKlass(), getParent());
    return creator.getNameOfAllCallsVector();
  }


  public Maybe<IASTName> getRegistrationVector() {
    AllCallsVectorFinderVisitor finder = new AllCallsVectorFinderVisitor();
    getKlass().accept(finder);
    return finder.getFoundCallsVector();
  }

  public Maybe<IASTName> getAllCallsVector() {
    for (IASTName optVector : getRegistrationVector()) {
      IBinding binding = optVector.resolveBinding();
      return head(list(getKlass().getTranslationUnit().getDefinitionsInAST(binding)));
    }

    return none();
  }

  public String getNameForExpectationVector() {
    return new ExpectedNameCreator(getName()).getNameForExpectationsVector();
  }

  public String getFqNameOfAllCallsVector() {
    AllCallsVectorNameCreator creator = new AllCallsVectorNameCreator(getKlass(), getParent());
    return creator.getFqNameOfAllCallsVector();
  }

  public MemFunMockSupportAdder getMockSupport(ASTRewrite rewriter, CppStandard std,
      ExistingTestDoubleMemFun memFun) {
    String callsVectorName = getNameOfAllCallsVector();
    PolymorphismKind polyKind = getPolymorphismKind();

    if (memFun.isConstructor())
      return new CtorMockSupport(rewriter, std, callsVectorName, polyKind);
    else
      return new MemFunMockSupport(rewriter, std, callsVectorName, polyKind);
  }
}
