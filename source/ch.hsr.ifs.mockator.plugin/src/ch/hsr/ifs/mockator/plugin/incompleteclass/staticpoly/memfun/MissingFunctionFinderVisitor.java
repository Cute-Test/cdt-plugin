package ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly.memfun;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPTemplateTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.TypeOfDependentExpression;

import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;

import ch.hsr.ifs.mockator.plugin.incompleteclass.StaticPolyMissingMemFun;


@SuppressWarnings("restriction")
class MissingFunctionFinderVisitor extends MissingMemFunVisitor {

   private final Collection<Function> unresolvedFunCalls;

   {
      shouldVisitNames = true;
   }

   public MissingFunctionFinderVisitor(final ICPPASTCompositeTypeSpecifier testDouble, final ICPPASTTemplateParameter templateParam,
                                       final ICPPASTTemplateDeclaration sut) {
      super(testDouble, templateParam, sut);
      unresolvedFunCalls = new LinkedHashSet<>();
   }

   @Override
   public Collection<? extends StaticPolyMissingMemFun> getMissingMemberFunctions() {
      return unresolvedFunCalls;
   }

   @Override
   public int visit(final IASTName name) {
      final IBinding binding = name.resolveBinding();

      if (!isMemFunReferenceToUnknownClass(binding)) { return PROCESS_CONTINUE; }

      if (isReferenceToTemplateParameter(binding)) {
         final ICPPASTFunctionCallExpression funCall = getFunctionCall(name);

         if (funCall != null) {
            final boolean isStaticFunCall = !isFieldReference(name);
            addToResultSet(funCall, isStaticFunCall);
            return PROCESS_SKIP;
         }
      }

      return PROCESS_CONTINUE;
   }

   private ICPPASTFunctionCallExpression getFunctionCall(final IASTName name) {
      return ASTUtil.getAncestorOfType(name, ICPPASTFunctionCallExpression.class);
   }

   private static boolean isMemFunReferenceToUnknownClass(final IBinding binding) {
      return binding instanceof CPPUnknownMethod;
   }

   private static boolean isFieldReference(final IASTName name) {
      return name.getParent() instanceof ICPPASTFieldReference;
   }

   private void addToResultSet(final ICPPASTFunctionCallExpression funCall, final boolean isStatic) {
      final Function newFunction = new Function(funCall, isStatic, templateParamType, getTestDoubleName());
      unresolvedFunCalls.add(newFunction);
   }

   private boolean isReferenceToTemplateParameter(final IBinding binding) {
      IType type = null;

      if (!(binding instanceof CPPTemplateTypeParameter)) {
         type = ((CPPUnknownMethod) binding).getOwnerType();
      }

      if (type instanceof TypeOfDependentExpression) {
         type = resolveTypeOfDependentExpression((TypeOfDependentExpression) type);
      }

      return resolvesToTemplateParam(type);
   }
}
