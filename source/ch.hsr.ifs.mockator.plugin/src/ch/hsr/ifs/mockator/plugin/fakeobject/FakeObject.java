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

   public FakeObject(final ICPPASTCompositeTypeSpecifier klass) {
      super(klass);
   }

   @Override
   public void addAdditionalCtorSupport(final ICPPASTFunctionDefinition defaultCtor, final CppStandard cppStd) {}

   @Override
   public DefaultCtorProvider getDefaultCtorProvider(final CppStandard cppStd) {
      return new FakeObjectDefaultCtorProvider(getKlass());
   }

   @Override
   protected TestDoubleMemFunImplStrategy getImplStrategy(final CppStandard cppStd) {
      return new FakeObjectMemFunImplStrategy();
   }

   @Override
   public void addToNamespace(final ICPPASTNamespaceDefinition parentNs, final IASTSimpleDeclaration testDouble,
         final ICPPASTCompositeTypeSpecifier testDoubleToMove, final CppStandard cppStd, final ASTRewrite rewriter) {
      parentNs.addDeclaration(testDouble);
   }
}
