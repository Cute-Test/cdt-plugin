package ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;

import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemFunFinder;
import ch.hsr.ifs.mockator.plugin.incompleteclass.StaticPolyMissingMemFun;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.PublicMemFunFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.ReferencingTestFunFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionEquivalenceVerifier;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionEquivalenceVerifier.ConstStrategy;
import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;


public class StaticPolyMissingMemFunFinder implements MissingMemFunFinder {

   private final ICProject cProject;
   private final IIndex    index;

   public StaticPolyMissingMemFunFinder(final ICProject cProject, final IIndex index) {
      this.cProject = cProject;
      this.index = index;
   }

   @Override
   public Collection<StaticPolyMissingMemFun> findMissingMemberFunctions(final ICPPASTCompositeTypeSpecifier klass) {
      final StaticPolymorphismUseFinder staticPolyFinder = getStaticPolyUseFinder(klass);
      final Set<StaticPolyMissingMemFun> missingMemFuns = orderPreservingSet();

      for (final IASTFunctionDefinition testFun : getReferencingTestFunctions(klass)) {
         final Collection<StaticPolyMissingMemFun> usedMemFunsInSut = staticPolyFinder.apply(testFun);
         final Collection<StaticPolyMissingMemFun> onlyMissing = filterAlreadyExisting(usedMemFunsInSut, klass);
         missingMemFuns.addAll(onlyMissing);
      }

      return missingMemFuns;
   }

   private StaticPolymorphismUseFinder getStaticPolyUseFinder(final ICPPASTCompositeTypeSpecifier klass) {
      return new StaticPolymorphismUseFinder(klass, cProject, index);
   }

   private static Collection<StaticPolyMissingMemFun> filterAlreadyExisting(final Collection<StaticPolyMissingMemFun> candidates,
         final ICPPASTCompositeTypeSpecifier klass) {
      final Collection<ICPPASTFunctionDefinition> existingMemFuns = getPublicMemberFunctions(klass);
      final List<StaticPolyMissingMemFun> onlyMissing = list();

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

   private static Collection<ICPPASTFunctionDefinition> getPublicMemberFunctions(final ICPPASTCompositeTypeSpecifier klass) {
      final PublicMemFunFinder finder = new PublicMemFunFinder(klass, PublicMemFunFinder.ALL_TYPES);
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
