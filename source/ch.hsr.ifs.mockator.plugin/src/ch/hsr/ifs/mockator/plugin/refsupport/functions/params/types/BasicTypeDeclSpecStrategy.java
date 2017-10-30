package ch.hsr.ifs.mockator.plugin.refsupport.functions.params.types;

import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;


@SuppressWarnings("restriction")
class BasicTypeDeclSpecStrategy implements DeclSpecGeneratorStrategy {

   private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();

   @Override
   public ICPPASTDeclSpecifier createDeclSpec(IType type) {
      Assert.instanceOf(type, IBasicType.class, "This strategy can only handle basic types");
      ICPPASTSimpleDeclSpecifier simpleDeclSpec = nodeFactory.newSimpleDeclSpecifier();
      IBasicType basicType = (IBasicType) type;
      simpleDeclSpec.setType(basicType.getKind());
      simpleDeclSpec.setSigned(basicType.isSigned());
      simpleDeclSpec.setUnsigned(basicType.isUnsigned());
      return simpleDeclSpec;
   }
}
