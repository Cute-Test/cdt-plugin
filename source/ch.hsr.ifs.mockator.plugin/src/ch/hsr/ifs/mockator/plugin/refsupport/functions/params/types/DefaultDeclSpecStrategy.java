package ch.hsr.ifs.mockator.plugin.refsupport.functions.params.types;

import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;


@SuppressWarnings("restriction")
class DefaultDeclSpecStrategy implements DeclSpecGeneratorStrategy {

   private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();

   @Override
   public ICPPASTDeclSpecifier createDeclSpec(final IType type) {
      final ICPPASTSimpleDeclSpecifier spec = nodeFactory.newSimpleDeclSpecifier();
      spec.setType(IASTSimpleDeclSpecifier.t_int);
      spec.setConst(true);
      return spec;
   }
}
