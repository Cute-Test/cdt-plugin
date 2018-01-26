package ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype;

import static ch.hsr.ifs.iltis.core.collections.CollectionHelper.list;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;

import ch.hsr.ifs.iltis.core.functional.OptHelper;
import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;

import ch.hsr.ifs.mockator.plugin.base.data.Pair;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.NameFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.BindingTypeVerifier;


class FunCallInjectionInfoCollector extends AbstractDepInjectInfoCollector {

   public FunCallInjectionInfoCollector(final IIndex index, final ICProject cProject) {
      super(index, cProject);
   }

   @Override
   public Optional<Pair<IASTName, IType>> collectDependencyInfos(final IASTName problemArgName) {
      final ICPPASTFunctionCallExpression funCall = ASTUtil.getAncestorOfType(problemArgName, ICPPASTFunctionCallExpression.class);

      if (funCall == null) {
         return Optional.empty();
      }

      final int args = getArgPosOfProblemType(problemArgName, list(funCall.getArguments()));

      return OptHelper.returnIfPresentElseEmpty(findMatchingFunction(getCandidateBindings(funCall), funCall, args), (
               match) -> getTargetClassOfProblemType(match, args));
   }

   private Optional<ICPPASTFunctionDeclarator> findMatchingFunction(final IBinding[] functions, final ICPPASTFunctionCallExpression funCall,
            final int argPosOfProblemType) {
      for (final IBinding fun : functions) {

         final Optional<ICPPASTFunctionDeclarator> funDecl = lookup.findFunctionDeclaration(fun, index);
         if (funDecl.isPresent() && areEquivalentExceptProblemType(list(funCall.getArguments()), funDecl.get(), argPosOfProblemType)) {
            return funDecl;
         }
      }
      return Optional.empty();
   }

   private static IBinding[] getCandidateBindings(final IASTFunctionCallExpression caller) {
      final IASTExpression funNameExpr = caller.getFunctionNameExpression();

      return OptHelper.returnIfPresentElse(findProblemBinding(funNameExpr), (problem) -> {
         final IProblemBinding iProblemBinding = (IProblemBinding) problem.resolveBinding();
         return iProblemBinding.getCandidateBindings();
      }, () -> new IBinding[] {});
   }

   private static Optional<IASTName> findProblemBinding(final IASTExpression funNameExpr) {
      final NameFinder finder = new NameFinder(funNameExpr);
      return finder.getNameMatchingCriteria((name) -> isProblemBinding(name.resolveBinding()));
   }

   private static boolean isProblemBinding(final IBinding binding) {
      return BindingTypeVerifier.isOfType(binding, IProblemBinding.class);
   }
}
