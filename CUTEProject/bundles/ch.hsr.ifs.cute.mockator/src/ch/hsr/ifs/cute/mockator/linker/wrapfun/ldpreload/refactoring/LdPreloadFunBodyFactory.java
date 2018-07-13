package ch.hsr.ifs.cute.mockator.linker.wrapfun.ldpreload.refactoring;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;


class LdPreloadFunBodyFactory {

   public LdPreloadFunBodyStrategy getFunBodyStrategy(final ICPPASTFunctionDeclarator funDecl) {
      if (isFreeFunction(funDecl)) return new FreeFunBodyStrategy();

      return new MemFunBodyStrategy();
   }

   private static boolean isFreeFunction(final ICPPASTFunctionDeclarator funDecl) {
      return CPPVisitor.findAncestorWithType(funDecl, ICPPASTCompositeTypeSpecifier.class).orElse(null) == null;
   }
}
