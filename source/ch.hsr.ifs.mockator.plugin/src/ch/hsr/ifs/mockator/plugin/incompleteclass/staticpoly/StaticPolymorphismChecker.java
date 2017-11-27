package ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly;

import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;

import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.ast.checker.CheckerResult;
import ch.hsr.ifs.iltis.cpp.ast.visitor.SimpleVisitor;
import ch.hsr.ifs.mockator.plugin.base.misc.IdHelper.ProblemId;
import ch.hsr.ifs.mockator.plugin.incompleteclass.checker.AbstractMissingMemFunChecker;


public class StaticPolymorphismChecker extends AbstractMissingMemFunChecker {

   @Override
   protected ASTVisitor getVisitor() {
      return new OnEachClass(this::markIfHasMissingMemFuns);
   }

   private class OnEachClass extends SimpleVisitor<ProblemId> {

      public OnEachClass(final Consumer<CheckerResult<ProblemId>> callback) {
         super(callback);
      }

      {
         shouldVisitDeclSpecifiers = true;
      }

      @Override
      public int visit(final IASTDeclSpecifier specifier) {
         if (!ASTUtil.isClass(specifier)) {
            return PROCESS_CONTINUE;
         }

         final ICPPASTCompositeTypeSpecifier clazz = (ICPPASTCompositeTypeSpecifier) specifier;

         if (isNonTemplateClass(clazz)) {
            report(getProblemId(), clazz);
         }

         return PROCESS_CONTINUE;
      }
   }

   private static boolean isNonTemplateClass(final ICPPASTCompositeTypeSpecifier clazz) {
      return ASTUtil.getAncestorOfType(clazz, ICPPASTTemplateDeclaration.class) == null;
   }

   @Override
   protected StaticPolyMissingMemFunFinder getMissingMemFunsFinder() {
      return new StaticPolyMissingMemFunFinder(getCProject(), getIndex());
   }

   @Override
   protected ProblemId getProblemId() {
      return ProblemId.STATIC_POLY_MISSING_MEMFUNS_IMPL;
   }

   @Override
   protected Optional<IASTName> getNameToMark(final ICPPASTCompositeTypeSpecifier clazz) {
      return Optional.of(clazz.getName());
   }
}
