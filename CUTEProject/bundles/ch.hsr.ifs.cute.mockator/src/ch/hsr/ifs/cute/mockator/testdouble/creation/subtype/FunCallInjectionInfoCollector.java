package ch.hsr.ifs.cute.mockator.testdouble.creation.subtype;

import static ch.hsr.ifs.iltis.core.core.collections.CollectionUtil.list;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.cute.mockator.refsupport.finder.NameFinder;
import ch.hsr.ifs.cute.mockator.refsupport.utils.BindingTypeVerifier;


class FunCallInjectionInfoCollector extends AbstractDepInjectInfoCollector {

   public FunCallInjectionInfoCollector(final IIndex index, final ICProject cProject) {
      super(index, cProject);
   }

   @Override
   public Optional<DependencyInfo> collectDependencyInfos(final IASTName problemArgName) {
      final ICPPASTFunctionCallExpression funCall = CPPVisitor.findAncestorWithType(problemArgName, ICPPASTFunctionCallExpression.class).orElse(null);

      if (funCall == null) { return Optional.empty(); }

      final int args = getArgPosOfProblemType(problemArgName, list(funCall.getArguments()));
      return findMatchingFunction(getCandidateBindings(funCall), funCall, args).flatMap(match -> getTargetClassOfProblemType(match, args));
   }

   private Optional<ICPPASTFunctionDeclarator> findMatchingFunction(final IBinding[] functions, final ICPPASTFunctionCallExpression funCall,
         final int argPosOfProblemType) {
      for (final IBinding fun : functions) {

         final Optional<ICPPASTFunctionDeclarator> funDecl = lookup.findFunctionDeclaration(fun, index);
         if (funDecl.isPresent() && areEquivalentExceptProblemType(list(funCall.getArguments()), funDecl.get(),
               argPosOfProblemType)) { return funDecl; }
      }
      return Optional.empty();
   }

   private static IBinding[] getCandidateBindings(final IASTFunctionCallExpression caller) {
      final IASTExpression funNameExpr = caller.getFunctionNameExpression();

      return findProblemBinding(funNameExpr).map(problem -> ((IProblemBinding) problem.resolveBinding()).getCandidateBindings()).orElse(
            new IBinding[0]);
   }

   private static Optional<IASTName> findProblemBinding(final IASTExpression funNameExpr) {
      final NameFinder finder = new NameFinder(funNameExpr);
      return finder.getNameMatchingCriteria((name) -> isProblemBinding(name.resolveBinding()));
   }

   private static boolean isProblemBinding(final IBinding binding) {
      return BindingTypeVerifier.isOfType(binding, IProblemBinding.class);
   }
}
