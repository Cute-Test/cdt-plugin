package ch.hsr.ifs.mockator.plugin.extractinterface.preconditions;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;
import static ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionEquivalenceVerifier.ConstStrategy.ConsiderConst;
import static ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionEquivalenceVerifier.ConstStrategy.IgnoreConst;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

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

import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.PublicMemFunFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.PublicMemFunFinder.Types;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionEquivalenceVerifier;
import ch.hsr.ifs.mockator.plugin.refsupport.lookup.NodeLookup;
import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;

public class MemFunCollector implements Consumer<ExtractInterfaceContext> {

   @Override
   public void accept(final ExtractInterfaceContext context) {
      final Collection<IASTDeclaration> publicMemFuns = getPublicMemFuns(context.getChosenClass());
      context.setAvailablePubMemFuns(publicMemFuns);
      context.setUsedPublicMemFuns(getUsedMemFuns(context, publicMemFuns));
   }

   private static Collection<IASTDeclaration> getPublicMemFuns(final ICPPASTCompositeTypeSpecifier klass) {
      final EnumSet<Types> onlyInstanceMemFuns = EnumSet.noneOf(PublicMemFunFinder.Types.class);
      final PublicMemFunFinder finder = new PublicMemFunFinder(klass, onlyInstanceMemFuns);
      return finder.getPublicMemFuns();
   }

   private static Collection<IASTDeclaration> getUsedMemFuns(final ExtractInterfaceContext context, final Collection<IASTDeclaration> publicMemFuns) {
      if (!considerOnlyReferencedMemFuns(context)) { return publicMemFuns; }

      final Collection<ICPPASTFunctionDefinition> functionsToAnalyse = getFunctionsToAnalyse(context);
      final IType dependencyType = getTypeOfDependency(context.getSelectedName());
      final Collection<ICPPASTFunctionCallExpression> funCalls = getFunCallsOnDependency(functionsToAnalyse, dependencyType);
      return filterUsed(funCalls, publicMemFuns, dependencyType);
   }

   private static Collection<ICPPASTFunctionDefinition> getFunctionsToAnalyse(final ExtractInterfaceContext context) {
      final Set<ICPPASTFunctionDefinition> functions = orderPreservingSet();

      for (final ICPPASTFunctionDeclarator funDecl : getAllMemberFunctions(context.getSutClass())) {
         if (isDeclarationDefinition(funDecl)) {
            final ICPPASTFunctionDefinition fun = (ICPPASTFunctionDefinition) funDecl.getParent();
            if (fun != null) {
               functions.add(fun);
            }
         } else {
            lookupDefinition(context, funDecl).ifPresent((function) -> functions.add(function));
         }
      }

      return functions;
   }

   private static Collection<ICPPASTFunctionCallExpression> getFunCallsOnDependency(final Collection<ICPPASTFunctionDefinition> functionsToAnalyze,
         final IType typeOfSelection) {
      final MemFunCallFinder memFunCallFinder = new MemFunCallFinder(typeOfSelection);

      for (final ICPPASTFunctionDefinition fun : functionsToAnalyze) {
         fun.accept(memFunCallFinder);
      }

      final Set<ICPPASTFunctionCallExpression> funCalls = orderPreservingSet();
      funCalls.addAll(memFunCallFinder.getReferencedCalls());
      return funCalls;
   }

   private static IType getTypeOfDependency(final IASTNode dependency) {
      final IASTSimpleDeclaration decl = ASTUtil.getAncestorOfType(dependency, IASTSimpleDeclaration.class);
      return CPPVisitor.createType(decl.getDeclSpecifier());
   }

   private static boolean considerOnlyReferencedMemFuns(final ExtractInterfaceContext context) {
      return context.getSutClass() != null;
   }

   private static Optional<ICPPASTFunctionDefinition> lookupDefinition(final ExtractInterfaceContext context,
         final ICPPASTFunctionDeclarator funDecl) {
      final NodeLookup lookup = new NodeLookup(context.getCProject(), context.getProgressMonitor());
      return lookup.findFunctionDefinition(funDecl.getName(), context.getCRefContext());
   }

   private static boolean isDeclarationDefinition(final ICPPASTFunctionDeclarator funDecl) {
      return funDecl.getParent() instanceof ICPPASTFunctionDefinition;
   }

   private static Collection<IASTDeclaration> filterUsed(final Collection<ICPPASTFunctionCallExpression> funCalls,
         final Collection<IASTDeclaration> availableFunctions, final IType typeOfSelection) {
      final List<IASTDeclaration> usedFuns = list();
      final FunctionEquivalenceVerifier.ConstStrategy strategy = getConstStrategy(typeOfSelection);

      for (final IASTDeclaration fun : availableFunctions) {
         final FunctionEquivalenceVerifier checker = new FunctionEquivalenceVerifier(getDeclarator(fun));

         for (final ICPPASTFunctionCallExpression call : funCalls) {
            if (checker.isEquivalent(call, strategy)) {
               usedFuns.add(fun);
               break;
            }
         }
      }

      return usedFuns;
   }

   private static FunctionEquivalenceVerifier.ConstStrategy getConstStrategy(final IType typeOfSelection) {
      return ASTUtil.hasConstPart(typeOfSelection) ? ConsiderConst : IgnoreConst;
   }

   private static ICPPASTFunctionDeclarator getDeclarator(final IASTDeclaration function) {
      return ASTUtil.getChildOfType(function, IASTFunctionDeclarator.class);
   }

   private static Collection<ICPPASTFunctionDeclarator> getAllMemberFunctions(final ICPPASTCompositeTypeSpecifier klass) {
      final List<ICPPASTFunctionDeclarator> allMemFuns = list();
      klass.accept(new ASTVisitor() {

         {
            shouldVisitDeclarations = true;
         }

         @Override
         public int visit(final IASTDeclaration decl) {
            final ICPPASTFunctionDeclarator candidate = ASTUtil.getChildOfType(decl, ICPPASTFunctionDeclarator.class);

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
      private final IType                              typeOfSelection;

      {
         shouldVisitExpressions = true;
      }

      public MemFunCallFinder(final IType typeOfSelection) {
         funCalls = orderPreservingSet();
         this.typeOfSelection = typeOfSelection;
      }

      public Collection<ICPPASTFunctionCallExpression> getReferencedCalls() {
         return funCalls;
      }

      @Override
      public int visit(final IASTExpression expression) {
         if (!(expression instanceof ICPPASTFunctionCallExpression)) { return PROCESS_CONTINUE; }

         final ICPPASTFunctionCallExpression funCall = (ICPPASTFunctionCallExpression) expression;
         final IASTExpression nameExpr = funCall.getFunctionNameExpression();

         if (nameExpr instanceof ICPPASTFieldReference) {
            final ICPPASTFieldReference reference = (ICPPASTFieldReference) nameExpr;
            processFieldReference(funCall, reference);
         }

         return PROCESS_CONTINUE;
      }

      private void processFieldReference(final ICPPASTFunctionCallExpression funCall, final ICPPASTFieldReference field) {
         final IASTExpression owner = field.getFieldOwner();
         final IType typeOfInstance = ASTUtil.unwindPointerOrRefType(owner.getExpressionType());

         if (hasSameTypeAsSelection(typeOfInstance)) {
            funCalls.add(funCall);
         }
      }

      private boolean hasSameTypeAsSelection(final IType typeOfInstance) {
         return ASTUtil.isSameType(typeOfInstance, typeOfSelection);
      }
   }
}
