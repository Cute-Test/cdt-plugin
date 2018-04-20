package ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly.memfun;

import java.util.Collection;

import org.eclipse.cdt.codan.core.cxx.CxxAstUtils;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPField;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalMemberAccess;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalTypeId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.EvalUnary;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.TypeOfDependentExpression;

import ch.hsr.ifs.iltis.cpp.core.ast.ASTUtil;

import ch.hsr.ifs.mockator.plugin.incompleteclass.StaticPolyMissingMemFun;


@SuppressWarnings("restriction")
abstract class MissingMemFunVisitor extends ASTVisitor {

   protected final ICPPASTTemplateDeclaration  sut;
   protected final IType                       templateParamType;
   private final ICPPASTCompositeTypeSpecifier testDouble;

   public MissingMemFunVisitor(final ICPPASTCompositeTypeSpecifier testDouble, final ICPPASTTemplateParameter templateParam,
                               final ICPPASTTemplateDeclaration sut) {
      this.testDouble = testDouble;
      this.sut = sut;
      templateParamType = getType(templateParam);
   }

   private static IType getType(final ICPPASTTemplateParameter param) {
      final IASTName name = ((ICPPASTSimpleTypeTemplateParameter) param).getName();
      return (IType) name.resolveBinding();
   }

   protected String getTemplateParamName() {
      return ((ICPPTemplateParameter) templateParamType).getName();
   }

   protected String getTestDoubleName() {
      return testDouble.getName().toString();
   }

   protected boolean resolvesToTemplateParam(final IType type) {
      final IType resolvedType = CxxAstUtils.unwindTypedef(type);
      IType unwoundType = ASTUtil.unwindPointerOrRefType(resolvedType);

      if (unwoundType == null) { return false; }

      if (unwoundType instanceof TypeOfDependentExpression) {
         final ICPPEvaluation evaluation = ((TypeOfDependentExpression) unwoundType).getEvaluation();
         if (evaluation instanceof EvalTypeId) {
            unwoundType = ((EvalTypeId) evaluation).getInputType();
         }
      }

      return ASTUtil.isSameType(unwoundType, templateParamType);
   }

   protected IType resolveTypeOfDependentExpression(final TypeOfDependentExpression type) {
      IType evalType = type.getEvaluation().getType();
      final ICPPEvaluation evaluation = ((TypeOfDependentExpression) evalType).getEvaluation();

      if (evaluation instanceof EvalMemberAccess) {
         final IBinding binding = ((EvalMemberAccess) evaluation).getMember();

         if (binding instanceof CPPField) {
            evalType = ((CPPField) binding).getType();
         }
      } else if (evaluation instanceof EvalUnary) {
         final ICPPEvaluation argument = ((EvalUnary) evaluation).getArgument();
         evalType = argument.getType();
      }

      return evalType;
   }

   public abstract Collection<? extends StaticPolyMissingMemFun> getMissingMemberFunctions();
}
