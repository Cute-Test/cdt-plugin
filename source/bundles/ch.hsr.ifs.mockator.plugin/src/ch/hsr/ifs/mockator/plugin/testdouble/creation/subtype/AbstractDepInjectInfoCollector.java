package ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.NullProgressMonitor;

import ch.hsr.ifs.iltis.core.functional.OptionalUtil;
import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;

import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.FunArgumentsTypeCollector;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.FunctionParamTypeCollector;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.ParamTypeEquivalenceTester;
import ch.hsr.ifs.mockator.plugin.refsupport.lookup.NodeLookup;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.TypeCreator;


abstract class AbstractDepInjectInfoCollector implements DepInjectInfoCollector {

   protected final IIndex     index;
   protected final NodeLookup lookup;

   public AbstractDepInjectInfoCollector(final IIndex index, final ICProject cProject) {
      this.index = index;
      lookup = new NodeLookup(cProject, new NullProgressMonitor());
   }

   protected int getArgPosOfProblemType(final IASTName name, final Collection<IASTInitializerClause> arguments) {
      int argPos = 0;

      for (final IASTInitializerClause arg : arguments) {
         if (isProblemArgumentWithSameName(name, arg)) {
            break;
         }

         argPos++;
      }

      return argPos;
   }

   protected Optional<DependencyInfo> getTargetClassOfProblemType(final ICPPASTFunctionDeclarator funDecl, final int argPosOfProblemType) {
      if (funDecl == null || funDecl.getParameters() == null || funDecl.getParameters().length <= argPosOfProblemType) { return Optional.empty(); }

      final ICPPASTParameterDeclaration paramForProblemArg = funDecl.getParameters()[argPosOfProblemType];
      final IType paramType = TypeCreator.byDeclarator(paramForProblemArg.getDeclarator());
      final IASTDeclSpecifier declSpecifier = paramForProblemArg.getDeclSpecifier();

      if (!(declSpecifier instanceof ICPPASTNamedTypeSpecifier)) { return Optional.empty(); }

      final ICPPASTNamedTypeSpecifier namedType = (ICPPASTNamedTypeSpecifier) declSpecifier;

      return OptionalUtil.returnIfPresentElseEmpty(findClassDefinitionOfProblemType(namedType.getName()), (clazz) -> Optional.of(new DependencyInfo(
            clazz.getName(), paramType)));
   }

   private Optional<ICPPASTCompositeTypeSpecifier> findClassDefinitionOfProblemType(final IASTName problemTypeName) {
      return lookup.findClassDefinition(problemTypeName.resolveBinding(), index);
   }

   private static boolean isProblemArgumentWithSameName(final IASTName name, final IASTInitializerClause initializer) {
      if (!(initializer instanceof IASTIdExpression)) { return false; }

      final IASTName argName = ((IASTIdExpression) initializer).getName();
      return argName.resolveBinding() instanceof IProblemBinding && argName.toString().equals(name.toString());
   }

   protected boolean areEquivalentExceptProblemType(final Collection<IASTInitializerClause> funArgs, final ICPPASTFunctionDeclarator funDecl,
         final int posToIgnore) {
      final FunArgumentsTypeCollector extractor = new FunArgumentsTypeCollector(funArgs);
      final ParamTypeEquivalenceTester tester = new ParamTypeEquivalenceTester(extractor.getFunArgTypes(), getTypesOfFunDecl(funDecl), (pos,
            receiverType) -> pos == posToIgnore && isPointerOrReferenceToClass(receiverType) && isConsideredAsBaseClass(receiverType));
      return tester.areParametersEquivalent();
   }

   private static boolean isPointerOrReferenceToClass(final IType type) {
      if (!ASTUtil.hasPointerOrRefType(type)) { return false; }

      IType underlyingType = ASTUtil.unwindPointerOrRefType(type);

      if (underlyingType instanceof IQualifierType) {
         underlyingType = ((IQualifierType) underlyingType).getType();
      }

      return underlyingType instanceof ICPPClassType;
   }

   private static List<IType> getTypesOfFunDecl(final ICPPASTFunctionDeclarator funDecl) {
      final FunctionParamTypeCollector helper = new FunctionParamTypeCollector(funDecl);
      return helper.getParameterTypes();
   }

   private static boolean isConsideredAsBaseClass(final IType missingArgType) {
      final IType type = ASTUtil.windDownToRealType(missingArgType, false);

      if (!(type instanceof ICPPClassType)) { return false; }

      return new BaseClassCandidateVerifier((ICPPClassType) type).isConsideredAsBaseClass();
   }
}
