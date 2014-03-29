package ch.hsr.ifs.mockator.plugin.testdouble.entities;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFunImplStrategy;
import ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly.memfun.AbstractStaticPolyMissingMemFun;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionEquivalenceVerifier.ConstStrategy;
import ch.hsr.ifs.mockator.plugin.testdouble.PolymorphismKind;
import ch.hsr.ifs.mockator.plugin.testdouble.support.BaseClassCtorCallHandler;

@SuppressWarnings("restriction")
public class DefaultConstructor extends AbstractStaticPolyMissingMemFun {
  private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();
  private final TestDouble testDouble;

  public DefaultConstructor(TestDouble testDouble) {
    this.testDouble = testDouble;
  }

  @Override
  public ICPPASTFunctionDefinition createFunctionDefinition(TestDoubleMemFunImplStrategy strategy,
      CppStandard cppStd) {
    ICPPASTFunctionDefinition defaultCtor = super.createFunctionDefinition(strategy, cppStd);
    addBaseClassCtorCallIfNecessary(defaultCtor, cppStd);
    addAdditionalCtorSupport(defaultCtor, cppStd);
    return defaultCtor;
  }

  @Override
  protected ICPPASTFunctionDeclarator createFunDecl() {
    IASTName ctorName = createNewCtorName();
    return nodeFactory.newFunctionDeclarator(ctorName);
  }

  @Override
  protected ICPPASTDeclSpecifier createReturnType(ICPPASTFunctionDeclarator funDecl) {
    return createCtorReturnType();
  }

  @Override
  protected IASTCompoundStatement createFunBody(TestDoubleMemFunImplStrategy strategy,
      ICPPASTFunctionDeclarator funDecl, ICPPASTDeclSpecifier specifier, CppStandard cppStd) {
    return createEmptyFunBody();
  }

  @Override
  public Collection<IASTInitializerClause> createDefaultArguments(CppStandard cppStd,
      LinkedEditModeStrategy linkedEdit) {
    return list();
  }

  private void addAdditionalCtorSupport(ICPPASTFunctionDefinition defaultCtor, CppStandard cppStd) {
    testDouble.addAdditionalCtorSupport(defaultCtor, cppStd);
  }

  private void addBaseClassCtorCallIfNecessary(ICPPASTFunctionDefinition defaultCtor,
      CppStandard cppStd) {
    if (testDouble.getPolymorphismKind() != PolymorphismKind.SubTypePoly)
      return;

    BaseClassCtorCallHandler handler = new BaseClassCtorCallHandler(testDouble.getClassType());

    for (ICPPASTConstructorChainInitializer candidate : handler.getBaseClassInitializer(cppStd)) {
      defaultCtor.addMemberInitializer(candidate);
    }
  }

  private IASTName createNewCtorName() {
    return nodeFactory.newName(testDouble.getName().toCharArray());
  }

  @Override
  public boolean isCallEquivalent(ICPPASTFunctionDefinition function, ConstStrategy strategy) {
    return false;
  }

  @Override
  public boolean isStatic() {
    return false;
  }

  @Override
  protected IASTExpression getUnderlyingExpression() {
    return null;
  }
}
