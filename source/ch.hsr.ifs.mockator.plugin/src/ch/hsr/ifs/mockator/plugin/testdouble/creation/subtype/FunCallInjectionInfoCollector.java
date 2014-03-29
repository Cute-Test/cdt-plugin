package ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

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

import ch.hsr.ifs.mockator.plugin.base.functional.F1;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.base.tuples.Pair;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.NameFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.BindingTypeVerifier;

class FunCallInjectionInfoCollector extends AbstractDepInjectInfoCollector {

  public FunCallInjectionInfoCollector(IIndex index, ICProject cProject) {
    super(index, cProject);
  }

  @Override
  public Maybe<Pair<IASTName, IType>> collectDependencyInfos(IASTName problemArgName) {
    ICPPASTFunctionCallExpression funCall =
        AstUtil.getAncestorOfType(problemArgName, ICPPASTFunctionCallExpression.class);

    if (funCall == null)
      return none();

    int args = getArgPosOfProblemType(problemArgName, list(funCall.getArguments()));

    for (ICPPASTFunctionDeclarator optMatch : findMatchingFunction(getCandidateBindings(funCall),
        funCall, args))
      return getTargetClassOfProblemType(optMatch, args);

    return none();
  }

  private Maybe<ICPPASTFunctionDeclarator> findMatchingFunction(IBinding[] functions,
      ICPPASTFunctionCallExpression funCall, int argPosOfProblemType) {
    for (IBinding fun : functions) {
      for (ICPPASTFunctionDeclarator optFunDecl : lookup.findFunctionDeclaration(fun, index)) {
        if (areEquivalentExceptProblemType(list(funCall.getArguments()), optFunDecl,
            argPosOfProblemType))
          return maybe(optFunDecl);
      }
    }
    return none();
  }

  private static IBinding[] getCandidateBindings(IASTFunctionCallExpression caller) {
    IASTExpression funNameExpr = caller.getFunctionNameExpression();

    for (IASTName optProblem : findProblemBinding(funNameExpr)) {
      IProblemBinding iProblemBinding = (IProblemBinding) optProblem.resolveBinding();
      return iProblemBinding.getCandidateBindings();
    }
    return new IBinding[] {};
  }

  private static Maybe<IASTName> findProblemBinding(IASTExpression funNameExpr) {
    NameFinder finder = new NameFinder(funNameExpr);
    return finder.getNameMatchingCriteria(new F1<IASTName, Boolean>() {
      @Override
      public Boolean apply(IASTName name) {
        return isProblemBinding(name.resolveBinding());
      }
    });
  }

  private static boolean isProblemBinding(IBinding binding) {
    return BindingTypeVerifier.isOfType(binding, IProblemBinding.class);
  }
}
