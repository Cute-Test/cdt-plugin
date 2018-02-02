package ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly.memfun;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;

import ch.hsr.ifs.iltis.core.exception.ILTISException;

import ch.hsr.ifs.mockator.plugin.incompleteclass.StaticPolyMissingMemFun;


public class MissingMemFunCollector {

   private static final Set<Class<? extends MissingMemFunVisitor>> MISSING_MEM_FUNS_FINDER = new LinkedHashSet<>();
   private final ICPPASTTemplateDeclaration                        sut;
   private final ICPPASTCompositeTypeSpecifier                     testDouble;
   private final Collection<ICPPASTTemplateDeclaration>            templateFunctions;

   static {
      MISSING_MEM_FUNS_FINDER.add(MissingCtorFinderVisitor.class);
      MISSING_MEM_FUNS_FINDER.add(MissingFunctionFinderVisitor.class);
      MISSING_MEM_FUNS_FINDER.add(MissingOperatorFinderVisitor.class);
   }

   public MissingMemFunCollector(final ICPPASTTemplateDeclaration sut, final ICPPASTCompositeTypeSpecifier testDouble,
                                 final Collection<ICPPASTTemplateDeclaration> templateFunctions) {
      this.sut = sut;
      this.testDouble = testDouble;
      this.templateFunctions = templateFunctions;
   }

   public Collection<StaticPolyMissingMemFun> getMissingMemberFunctions(final ICPPASTTemplateParameter templateParameter) {
      final Collection<StaticPolyMissingMemFun> missingMemFuns = new LinkedHashSet<>();

      for (final Class<? extends MissingMemFunVisitor> visitor : MISSING_MEM_FUNS_FINDER) {
         collectMissingMemFuns(templateParameter, missingMemFuns, visitor);
      }

      return missingMemFuns;
   }

   private void collectMissingMemFuns(final ICPPASTTemplateParameter templateParameter, final Collection<StaticPolyMissingMemFun> missingMemFuns,
         final Class<? extends MissingMemFunVisitor> kindOfFinder) {
      final MissingMemFunVisitor memFunFinder = getMissingMemFunFinder(templateParameter, kindOfFinder);
      sut.accept(memFunFinder);
      collectInTemplateMemFuns(memFunFinder);
      missingMemFuns.addAll(memFunFinder.getMissingMemberFunctions());
   }

   private void collectInTemplateMemFuns(final MissingMemFunVisitor memFunFinder) {
      for (final ICPPASTTemplateDeclaration templateFun : templateFunctions) {
         templateFun.accept(memFunFinder);
      }
   }

   private MissingMemFunVisitor getMissingMemFunFinder(final ICPPASTTemplateParameter templateParam,
         final Class<? extends MissingMemFunVisitor> visitor) {
      try {
         final Constructor<?> ctor = visitor.getConstructor(ICPPASTCompositeTypeSpecifier.class, ICPPASTTemplateParameter.class,
               ICPPASTTemplateDeclaration.class);
         return (MissingMemFunVisitor) ctor.newInstance(testDouble, templateParam, sut);
      }
      catch (final Exception e) {
         throw new ILTISException(e).rethrowUnchecked();
      }
   }
}
