package ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly.memfun;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;

import java.util.Collection;

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

import ch.hsr.ifs.mockator.plugin.incompleteclass.StaticPolyMissingMemFun;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;


@SuppressWarnings("restriction")
class MissingFunctionFinderVisitor extends MissingMemFunVisitor {

   private final Collection<Function> unresolvedFunCalls;

   {
      shouldVisitNames = true;
   }

   public MissingFunctionFinderVisitor(ICPPASTCompositeTypeSpecifier testDouble, ICPPASTTemplateParameter templateParam,
                                       ICPPASTTemplateDeclaration sut) {
      super(testDouble, templateParam, sut);
      unresolvedFunCalls = orderPreservingSet();
   }

   @Override
   public Collection<? extends StaticPolyMissingMemFun> getMissingMemberFunctions() {
      return unresolvedFunCalls;
   }

   @Override
   public int visit(IASTName name) {
      IBinding binding = name.resolveBinding();

      if (!isMemFunReferenceToUnknownClass(binding)) return PROCESS_CONTINUE;

      if (isReferenceToTemplateParameter(binding)) {
         ICPPASTFunctionCallExpression funCall = getFunctionCall(name);

         if (funCall != null) {
            boolean isStaticFunCall = !isFieldReference(name);
            addToResultSet(funCall, isStaticFunCall);
            return PROCESS_SKIP;
         }
      }

      return PROCESS_CONTINUE;
   }

   private ICPPASTFunctionCallExpression getFunctionCall(IASTName name) {
      return AstUtil.getAncestorOfType(name, ICPPASTFunctionCallExpression.class);
   }

   private static boolean isMemFunReferenceToUnknownClass(IBinding binding) {
      return binding instanceof CPPUnknownMethod;
   }

   private static boolean isFieldReference(IASTName name) {
      return name.getParent() instanceof ICPPASTFieldReference;
   }

   private void addToResultSet(ICPPASTFunctionCallExpression funCall, boolean isStatic) {
      Function newFunction = new Function(funCall, isStatic, templateParamType, getTestDoubleName());
      unresolvedFunCalls.add(newFunction);
   }

   private boolean isReferenceToTemplateParameter(IBinding binding) {
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
