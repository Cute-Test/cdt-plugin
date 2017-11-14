package ch.hsr.ifs.mockator.plugin.refsupport.functions.params.types;

import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;


class BasicTypeDeclSpecStrategy implements DeclSpecGeneratorStrategy {

   @Override
   public ICPPASTDeclSpecifier createDeclSpec(final IType type) {
      Assert.instanceOf(type, IBasicType.class, "This strategy can only handle basic types");
      final ICPPASTSimpleDeclSpecifier simpleDeclSpec = nodeFactory.newSimpleDeclSpecifier();
      final IBasicType basicType = (IBasicType) type;
      simpleDeclSpec.setType(basicType.getKind());
      simpleDeclSpec.setSigned(basicType.isSigned());
      simpleDeclSpec.setUnsigned(basicType.isUnsigned());
      return simpleDeclSpec;
   }
}
