package ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;

import ch.hsr.ifs.iltis.cpp.core.ast.ASTUtil;

import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemFunFinder;
import ch.hsr.ifs.mockator.plugin.incompleteclass.StaticPolyMissingMemFun;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.PublicMemFunFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.ReferencingTestFunFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionEquivalenceVerifier;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionEquivalenceVerifier.ConstStrategy;


public class StaticPolyMissingMemFunFinder implements MissingMemFunFinder {

   private final ICProject cProject;
   private final IIndex    index;

   public StaticPolyMissingMemFunFinder(final ICProject cProject, final IIndex index) {
      this.cProject = cProject;
      this.index = index;
   }

   @Override
   public Collection<StaticPolyMissingMemFun> findMissingMemberFunctions(final ICPPASTCompositeTypeSpecifier clazz) {
      final StaticPolymorphismUseFinder staticPolyFinder = getStaticPolyUseFinder(clazz);
      final Set<StaticPolyMissingMemFun> missingMemFuns = new LinkedHashSet<>();

      for (final IASTFunctionDefinition testFun : getReferencingTestFunctions(clazz)) {
         final Collection<StaticPolyMissingMemFun> usedMemFunsInSut = staticPolyFinder.apply(testFun);
         final Collection<StaticPolyMissingMemFun> onlyMissing = filterAlreadyExisting(usedMemFunsInSut, clazz);
         missingMemFuns.addAll(onlyMissing);
      }

      return missingMemFuns;
   }

   private StaticPolymorphismUseFinder getStaticPolyUseFinder(final ICPPASTCompositeTypeSpecifier clazz) {
      return new StaticPolymorphismUseFinder(clazz, cProject, index);
   }

   private static Collection<StaticPolyMissingMemFun> filterAlreadyExisting(final Collection<StaticPolyMissingMemFun> candidates,
         final ICPPASTCompositeTypeSpecifier clazz) {
      final Collection<ICPPASTFunctionDefinition> existingMemFuns = getPublicMemberFunctions(clazz);
      final List<StaticPolyMissingMemFun> onlyMissing = new ArrayList<>();

      for (final StaticPolyMissingMemFun candidate : candidates) {
         boolean match = false;

         for (final ICPPASTFunctionDefinition existing : existingMemFuns) {
            if (functionAlreadyExists(candidate, existing)) {
               match = true;
               break;
            }
         }

         if (!match) {
            onlyMissing.add(candidate);
         }
      }
      return onlyMissing;
   }

   private static boolean functionAlreadyExists(final StaticPolyMissingMemFun missingMemFun, final ICPPASTFunctionDefinition function) {
      return missingMemFun.isCallEquivalent(function, getConstStrategy(function));
   }

   private static Collection<ICPPASTFunctionDefinition> getPublicMemberFunctions(final ICPPASTCompositeTypeSpecifier clazz) {
      final PublicMemFunFinder finder = new PublicMemFunFinder(clazz, PublicMemFunFinder.ALL_TYPES);
      return ASTUtil.getFunctionDefinitions(finder.getPublicMemFuns());
   }

   private Collection<ICPPASTFunctionDefinition> getReferencingTestFunctions(final ICPPASTCompositeTypeSpecifier refClass) {
      final ReferencingTestFunFinder finder = new ReferencingTestFunFinder(cProject, refClass);
      return finder.findInAst(refClass.getTranslationUnit());
   }

   private static ConstStrategy getConstStrategy(final ICPPASTFunctionDefinition function) {
      if (!ASTUtil.isStatic(function.getDeclSpecifier()) && !ASTUtil.isConstructor(function))
         return FunctionEquivalenceVerifier.ConstStrategy.ConsiderConst;

      return FunctionEquivalenceVerifier.ConstStrategy.IgnoreConst;
   }
}
