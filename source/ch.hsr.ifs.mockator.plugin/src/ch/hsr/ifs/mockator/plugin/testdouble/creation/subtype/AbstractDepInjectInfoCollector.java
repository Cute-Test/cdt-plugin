package ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import java.util.Collection;
import java.util.List;

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

import ch.hsr.ifs.mockator.plugin.base.functional.F2;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.base.tuples.Pair;
import ch.hsr.ifs.mockator.plugin.base.tuples.Tuple;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.FunArgumentsTypeCollector;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.FunctionParamTypeCollector;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.ParamTypeEquivalenceTester;
import ch.hsr.ifs.mockator.plugin.refsupport.lookup.NodeLookup;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.TypeCreator;

abstract class AbstractDepInjectInfoCollector implements DepInjectInfoCollector {
  protected final IIndex index;
  protected final NodeLookup lookup;

  public AbstractDepInjectInfoCollector(IIndex index, ICProject cProject) {
    this.index = index;
    lookup = new NodeLookup(cProject, new NullProgressMonitor());
  }

  protected int getArgPosOfProblemType(IASTName name, Collection<IASTInitializerClause> arguments) {
    int argPos = 0;

    for (IASTInitializerClause arg : arguments) {
      if (isProblemArgumentWithSameName(name, arg)) {
        break;
      }

      argPos++;
    }

    return argPos;
  }

  protected Maybe<Pair<IASTName, IType>> getTargetClassOfProblemType(
      ICPPASTFunctionDeclarator funDecl, int argPosOfProblemType) {
    ICPPASTParameterDeclaration paramForProblemArg = funDecl.getParameters()[argPosOfProblemType];
    IType paramType = TypeCreator.byDeclarator(paramForProblemArg.getDeclarator());
    IASTDeclSpecifier declSpecifier = paramForProblemArg.getDeclSpecifier();

    if (!(declSpecifier instanceof ICPPASTNamedTypeSpecifier))
      return none();

    ICPPASTNamedTypeSpecifier namedType = ((ICPPASTNamedTypeSpecifier) declSpecifier);

    for (ICPPASTCompositeTypeSpecifier optKlass : findClassDefinitionOfProblemType(namedType
        .getName()))
      return maybe(Tuple.from(optKlass.getName(), paramType));

    return none();
  }

  private Maybe<ICPPASTCompositeTypeSpecifier> findClassDefinitionOfProblemType(
      IASTName problemTypeName) {
    return lookup.findClassDefinition(problemTypeName.resolveBinding(), index);
  }

  private static boolean isProblemArgumentWithSameName(IASTName name,
      IASTInitializerClause initializer) {
    if (!(initializer instanceof IASTIdExpression))
      return false;

    IASTName argName = ((IASTIdExpression) initializer).getName();
    return argName.resolveBinding() instanceof IProblemBinding
        && argName.toString().equals(name.toString());
  }

  protected boolean areEquivalentExceptProblemType(Collection<IASTInitializerClause> funArgs,
      ICPPASTFunctionDeclarator funDecl, final int posToIgnore) {
    FunArgumentsTypeCollector extractor = new FunArgumentsTypeCollector(funArgs);
    ParamTypeEquivalenceTester tester =
        new ParamTypeEquivalenceTester(extractor.getFunArgTypes(), getTypesOfFunDecl(funDecl),
            new F2<Integer, IType, Boolean>() {
              @Override
              public Boolean apply(Integer pos, IType receiverType) {
                return pos == posToIgnore && isPointerOrReferenceToClass(receiverType)
                    && isConsideredAsBaseClass(receiverType);
              }
            });
    return tester.areParametersEquivalent();
  }

  private static boolean isPointerOrReferenceToClass(IType type) {
    if (!AstUtil.hasPointerOrRefType(type))
      return false;

    IType underlyingType = AstUtil.unwindPointerOrRefType(type);

    if (underlyingType instanceof IQualifierType) {
      underlyingType = ((IQualifierType) underlyingType).getType();
    }

    return underlyingType instanceof ICPPClassType;
  }

  private static List<IType> getTypesOfFunDecl(ICPPASTFunctionDeclarator funDecl) {
    FunctionParamTypeCollector helper = new FunctionParamTypeCollector(funDecl);
    return helper.getParameterTypes();
  }

  private static boolean isConsideredAsBaseClass(IType missingArgType) {
    IType type = AstUtil.windDownToRealType(missingArgType, false);

    if (!(type instanceof ICPPClassType))
      return false;

    return new BaseClassCandidateVerifier((ICPPClassType) type).isConsideredAsBaseClass();
  }
}
