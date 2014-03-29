package ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.incompleteclass.checker.AbstractMissingMemFunChecker;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

public class StaticPolymorphismChecker extends AbstractMissingMemFunChecker {
  public static final String STATIC_POLY_MISSING_MEMFUNS_IMPL_PROBLEM_ID =
      "ch.hsr.ifs.mockator.StaticPolyMissingMemFunsProblem";

  @Override
  protected ASTVisitor getAstVisitor() {
    return new OnEachClass();
  }

  private class OnEachClass extends ASTVisitor {
    {
      shouldVisitDeclSpecifiers = true;
    }

    @Override
    public int visit(IASTDeclSpecifier specifier) {
      if (!AstUtil.isClass(specifier))
        return PROCESS_CONTINUE;

      ICPPASTCompositeTypeSpecifier klass = (ICPPASTCompositeTypeSpecifier) specifier;

      if (isNonTemplateClass(klass)) {
        markIfHasMissingMemFuns(klass);
      }

      return PROCESS_CONTINUE;
    }
  }

  private static boolean isNonTemplateClass(ICPPASTCompositeTypeSpecifier klass) {
    return AstUtil.getAncestorOfType(klass, ICPPASTTemplateDeclaration.class) == null;
  }

  @Override
  protected StaticPolyMissingMemFunFinder getMissingMemFunsFinder() {
    return new StaticPolyMissingMemFunFinder(getCProject(), getIndex());
  }

  @Override
  protected String getProblemId() {
    return STATIC_POLY_MISSING_MEMFUNS_IMPL_PROBLEM_ID;
  }

  @Override
  protected Maybe<IASTName> getNameToMark(ICPPASTCompositeTypeSpecifier klass) {
    return maybe(klass.getName());
  }
}
