package ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly.memfun;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.incompleteclass.StaticPolyMissingMemFun;


public class MissingMemFunCollector {

   private static final Set<Class<? extends MissingMemFunVisitor>> MISSING_MEM_FUNS_FINDER = orderPreservingSet();
   private final ICPPASTTemplateDeclaration                        sut;
   private final ICPPASTCompositeTypeSpecifier                     testDouble;
   private final Collection<ICPPASTTemplateDeclaration>            templateFunctions;

   static {
      MISSING_MEM_FUNS_FINDER.add(MissingCtorFinderVisitor.class);
      MISSING_MEM_FUNS_FINDER.add(MissingFunctionFinderVisitor.class);
      MISSING_MEM_FUNS_FINDER.add(MissingOperatorFinderVisitor.class);
   }

   public MissingMemFunCollector(ICPPASTTemplateDeclaration sut, ICPPASTCompositeTypeSpecifier testDouble,
                                 Collection<ICPPASTTemplateDeclaration> templateFunctions) {
      this.sut = sut;
      this.testDouble = testDouble;
      this.templateFunctions = templateFunctions;
   }

   public Collection<StaticPolyMissingMemFun> getMissingMemberFunctions(ICPPASTTemplateParameter templateParameter) {
      Collection<StaticPolyMissingMemFun> missingMemFuns = orderPreservingSet();

      for (Class<? extends MissingMemFunVisitor> visitor : MISSING_MEM_FUNS_FINDER) {
         collectMissingMemFuns(templateParameter, missingMemFuns, visitor);
      }

      return missingMemFuns;
   }

   private void collectMissingMemFuns(ICPPASTTemplateParameter templateParameter, Collection<StaticPolyMissingMemFun> missingMemFuns,
         Class<? extends MissingMemFunVisitor> kindOfFinder) {
      MissingMemFunVisitor memFunFinder = getMissingMemFunFinder(templateParameter, kindOfFinder);
      sut.accept(memFunFinder);
      collectInTemplateMemFuns(memFunFinder);
      missingMemFuns.addAll(memFunFinder.getMissingMemberFunctions());
   }

   private void collectInTemplateMemFuns(MissingMemFunVisitor memFunFinder) {
      for (ICPPASTTemplateDeclaration templateFun : templateFunctions) {
         templateFun.accept(memFunFinder);
      }
   }

   private MissingMemFunVisitor getMissingMemFunFinder(ICPPASTTemplateParameter templateParam, Class<? extends MissingMemFunVisitor> visitor) {
      try {
         Constructor<?> ctor = visitor.getConstructor(ICPPASTCompositeTypeSpecifier.class, ICPPASTTemplateParameter.class,
               ICPPASTTemplateDeclaration.class);
         return (MissingMemFunVisitor) ctor.newInstance(testDouble, templateParam, sut);
      }
      catch (Exception e) {
         throw new MockatorException(e);
      }
   }
}
