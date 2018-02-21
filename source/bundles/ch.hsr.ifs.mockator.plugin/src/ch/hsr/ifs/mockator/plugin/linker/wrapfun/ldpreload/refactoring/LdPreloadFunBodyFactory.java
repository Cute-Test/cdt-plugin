package ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload.refactoring;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;

import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;


class LdPreloadFunBodyFactory {

   public LdPreloadFunBodyStrategy getFunBodyStrategy(final ICPPASTFunctionDeclarator funDecl) {
      if (isFreeFunction(funDecl)) return new FreeFunBodyStrategy();

      return new MemFunBodyStrategy();
   }

   private static boolean isFreeFunction(final ICPPASTFunctionDeclarator funDecl) {
      return ASTUtil.getAncestorOfType(funDecl, ICPPASTCompositeTypeSpecifier.class) == null;
   }
}
