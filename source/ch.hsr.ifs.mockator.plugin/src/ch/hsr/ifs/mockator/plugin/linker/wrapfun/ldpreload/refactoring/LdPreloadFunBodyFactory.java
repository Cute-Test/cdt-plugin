package ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload.refactoring;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;

import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

class LdPreloadFunBodyFactory {

  public LdPreloadFunBodyStrategy getFunBodyStrategy(ICPPASTFunctionDeclarator funDecl) {
    if (isFreeFunction(funDecl))
      return new FreeFunBodyStrategy();

    return new MemFunBodyStrategy();
  }

  private static boolean isFreeFunction(ICPPASTFunctionDeclarator funDecl) {
    return AstUtil.getAncestorOfType(funDecl, ICPPASTCompositeTypeSpecifier.class) == null;
  }
}
