package ch.hsr.ifs.mockator.plugin.fakeobject;

import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.mockator.plugin.incompleteclass.DefaultCtorProvider;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFunImplStrategy;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.AbstractTestDouble;

public class FakeObject extends AbstractTestDouble {

  public FakeObject(ICPPASTCompositeTypeSpecifier klass) {
    super(klass);
  }

  @Override
  public void addAdditionalCtorSupport(ICPPASTFunctionDefinition defaultCtor, CppStandard cppStd) {}

  @Override
  public DefaultCtorProvider getDefaultCtorProvider(CppStandard cppStd) {
    return new FakeObjectDefaultCtorProvider(getKlass());
  }

  @Override
  protected TestDoubleMemFunImplStrategy getImplStrategy(CppStandard cppStd) {
    return new FakeObjectMemFunImplStrategy();
  }

  @Override
  public void addToNamespace(ICPPASTNamespaceDefinition parentNs, IASTSimpleDeclaration testDouble,
      ICPPASTCompositeTypeSpecifier testDoubleToMove, CppStandard cppStd, ASTRewrite rewriter) {
    parentNs.addDeclaration(testDouble);
  }
}
