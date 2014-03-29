package ch.hsr.ifs.mockator.plugin.extractinterface.preconditions;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;
import static ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionEquivalenceVerifier.ConstStrategy.ConsiderConst;
import static ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionEquivalenceVerifier.ConstStrategy.IgnoreConst;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.PublicMemFunFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.PublicMemFunFinder.Types;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionEquivalenceVerifier;
import ch.hsr.ifs.mockator.plugin.refsupport.lookup.NodeLookup;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

@SuppressWarnings("restriction")
public class MemFunCollector implements F1V<ExtractInterfaceContext> {

  @Override
  public void apply(ExtractInterfaceContext context) {
    Collection<IASTDeclaration> publicMemFuns = getPublicMemFuns(context.getChosenClass());
    context.setAvailablePubMemFuns(publicMemFuns);
    context.setUsedPublicMemFuns(getUsedMemFuns(context, publicMemFuns));
  }

  private static Collection<IASTDeclaration> getPublicMemFuns(ICPPASTCompositeTypeSpecifier klass) {
    EnumSet<Types> onlyInstanceMemFuns = EnumSet.noneOf(PublicMemFunFinder.Types.class);
    PublicMemFunFinder finder = new PublicMemFunFinder(klass, onlyInstanceMemFuns);
    return finder.getPublicMemFuns();
  }

  private static Collection<IASTDeclaration> getUsedMemFuns(ExtractInterfaceContext context,
      Collection<IASTDeclaration> publicMemFuns) {
    if (!considerOnlyReferencedMemFuns(context))
      return publicMemFuns;

    Collection<ICPPASTFunctionDefinition> functionsToAnalyse = getFunctionsToAnalyse(context);
    IType dependencyType = getTypeOfDependency(context.getSelectedName());
    Collection<ICPPASTFunctionCallExpression> funCalls =
        getFunCallsOnDependency(functionsToAnalyse, dependencyType);
    return filterUsed(funCalls, publicMemFuns, dependencyType);
  }

  private static Collection<ICPPASTFunctionDefinition> getFunctionsToAnalyse(
      ExtractInterfaceContext context) {
    Set<ICPPASTFunctionDefinition> functions = orderPreservingSet();

    for (ICPPASTFunctionDeclarator funDecl : getAllMemberFunctions(context.getSutClass())) {
      if (isDeclarationDefinition(funDecl)) {
        ICPPASTFunctionDefinition fun = (ICPPASTFunctionDefinition) funDecl.getParent();
        if (fun != null) {
          functions.add(fun);
        }
      } else {
        for (ICPPASTFunctionDefinition optFunction : lookupDefinition(context, funDecl)) {
          functions.add(optFunction);
        }
      }
    }

    return functions;
  }

  private static Collection<ICPPASTFunctionCallExpression> getFunCallsOnDependency(
      Collection<ICPPASTFunctionDefinition> functionsToAnalyze, IType typeOfSelection) {
    MemFunCallFinder memFunCallFinder = new MemFunCallFinder(typeOfSelection);

    for (ICPPASTFunctionDefinition fun : functionsToAnalyze) {
      fun.accept(memFunCallFinder);
    }

    Set<ICPPASTFunctionCallExpression> funCalls = orderPreservingSet();
    funCalls.addAll(memFunCallFinder.getReferencedCalls());
    return funCalls;
  }

  private static IType getTypeOfDependency(IASTNode dependency) {
    IASTSimpleDeclaration decl = AstUtil.getAncestorOfType(dependency, IASTSimpleDeclaration.class);
    return CPPVisitor.createType(decl.getDeclSpecifier());
  }

  private static boolean considerOnlyReferencedMemFuns(ExtractInterfaceContext context) {
    return context.getSutClass() != null;
  }

  private static Maybe<ICPPASTFunctionDefinition> lookupDefinition(ExtractInterfaceContext context,
      ICPPASTFunctionDeclarator funDecl) {
    NodeLookup lookup = new NodeLookup(context.getCProject(), context.getProgressMonitor());
    return lookup.findFunctionDefinition(funDecl.getName(), context.getCRefContext());
  }

  private static boolean isDeclarationDefinition(ICPPASTFunctionDeclarator funDecl) {
    return funDecl.getParent() instanceof ICPPASTFunctionDefinition;
  }

  private static Collection<IASTDeclaration> filterUsed(
      Collection<ICPPASTFunctionCallExpression> funCalls,
      Collection<IASTDeclaration> availableFunctions, IType typeOfSelection) {
    List<IASTDeclaration> usedFuns = list();
    FunctionEquivalenceVerifier.ConstStrategy strategy = getConstStrategy(typeOfSelection);

    for (IASTDeclaration fun : availableFunctions) {
      FunctionEquivalenceVerifier checker = new FunctionEquivalenceVerifier(getDeclarator(fun));

      for (ICPPASTFunctionCallExpression call : funCalls) {
        if (checker.isEquivalent(call, strategy)) {
          usedFuns.add(fun);
          break;
        }
      }
    }

    return usedFuns;
  }

  private static FunctionEquivalenceVerifier.ConstStrategy getConstStrategy(IType typeOfSelection) {
    return AstUtil.hasConstPart(typeOfSelection) ? ConsiderConst : IgnoreConst;
  }

  private static ICPPASTFunctionDeclarator getDeclarator(IASTDeclaration function) {
    return AstUtil.getChildOfType(function, IASTFunctionDeclarator.class);
  }

  private static Collection<ICPPASTFunctionDeclarator> getAllMemberFunctions(
      ICPPASTCompositeTypeSpecifier klass) {
    final List<ICPPASTFunctionDeclarator> allMemFuns = list();
    klass.accept(new ASTVisitor() {
      {
        shouldVisitDeclarations = true;
      }

      @Override
      public int visit(IASTDeclaration decl) {
        ICPPASTFunctionDeclarator candidate =
            AstUtil.getChildOfType(decl, ICPPASTFunctionDeclarator.class);

        if (candidate != null) {
          allMemFuns.add(candidate);
        }

        return PROCESS_CONTINUE;
      }
    });
    return allMemFuns;
  }

  private static class MemFunCallFinder extends ASTVisitor {
    private final Set<ICPPASTFunctionCallExpression> funCalls;
    private final IType typeOfSelection;

    {
      shouldVisitExpressions = true;
    }

    public MemFunCallFinder(IType typeOfSelection) {
      funCalls = orderPreservingSet();
      this.typeOfSelection = typeOfSelection;
    }

    public Collection<ICPPASTFunctionCallExpression> getReferencedCalls() {
      return funCalls;
    }

    @Override
    public int visit(IASTExpression expression) {
      if (!(expression instanceof ICPPASTFunctionCallExpression))
        return PROCESS_CONTINUE;

      ICPPASTFunctionCallExpression funCall = (ICPPASTFunctionCallExpression) expression;
      IASTExpression nameExpr = funCall.getFunctionNameExpression();

      if (nameExpr instanceof ICPPASTFieldReference) {
        ICPPASTFieldReference reference = (ICPPASTFieldReference) nameExpr;
        processFieldReference(funCall, reference);
      }

      return PROCESS_CONTINUE;
    }

    private void processFieldReference(ICPPASTFunctionCallExpression funCall,
        ICPPASTFieldReference field) {
      IASTExpression owner = field.getFieldOwner();
      IType typeOfInstance = AstUtil.unwindPointerOrRefType(owner.getExpressionType());

      if (hasSameTypeAsSelection(typeOfInstance)) {
        funCalls.add(funCall);
      }
    }

    private boolean hasSameTypeAsSelection(IType typeOfInstance) {
      return AstUtil.isSameType(typeOfInstance, typeOfSelection);
    }
  }
}
