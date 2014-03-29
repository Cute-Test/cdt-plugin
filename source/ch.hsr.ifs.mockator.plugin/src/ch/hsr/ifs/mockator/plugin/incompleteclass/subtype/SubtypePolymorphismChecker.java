package ch.hsr.ifs.mockator.plugin.incompleteclass.subtype;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.head;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import org.eclipse.cdt.codan.core.cxx.CxxAstUtils;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemFunFinder;
import ch.hsr.ifs.mockator.plugin.incompleteclass.checker.AbstractMissingMemFunChecker;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

public class SubtypePolymorphismChecker extends AbstractMissingMemFunChecker {
  public static final String SUBTYPE_MISSING_MEMFUNS_IMPL_PROBLEM_ID =
      "ch.hsr.ifs.mockator.SubtypeMissingMemFunsProblem";

  @Override
  protected ASTVisitor getAstVisitor() {
    return new AbstractClassInstantiationFinder();
  }

  private class AbstractClassInstantiationFinder extends ASTVisitor {
    {
      shouldVisitDeclarations = true;
      shouldVisitExpressions = true;
    }

    @Override
    public int visit(IASTDeclaration declaration) {
      if (declaration instanceof IASTSimpleDeclaration) {
        IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) declaration;
        checkForClassInstantiation(simpleDecl);
      }

      return PROCESS_CONTINUE;
    }

    private void checkForClassInstantiation(IASTSimpleDeclaration simpleDecl) {
      IASTDeclSpecifier declSpec = simpleDecl.getDeclSpecifier();

      if (declSpec.getStorageClass() == IASTDeclSpecifier.sc_typedef)
        return;

      for (IASTDeclarator declarator : simpleDecl.getDeclarators()) {
        if (!AstUtil.hasPointerOrRefType(declarator)) {
          checkIfAbstract(declSpec);
          break;
        }
      }
    }

    @Override
    public int visit(IASTExpression expr) {
      if (expr instanceof ICPPASTNewExpression) {
        ICPPASTNewExpression newExpression = (ICPPASTNewExpression) expr;

        if (!AstUtil.hasPointerOrRefType(newExpression.getTypeId().getAbstractDeclarator())) {
          IASTDeclSpecifier declSpec = newExpression.getTypeId().getDeclSpecifier();

          if (declSpec instanceof ICPPASTNamedTypeSpecifier) {
            IASTName constructorName = ((ICPPASTNamedTypeSpecifier) declSpec).getName();
            checkIfAbstract(constructorName);
          }
        }
      } else if (expr instanceof ICPPASTFunctionCallExpression) {
        ICPPASTFunctionCallExpression funCall = (ICPPASTFunctionCallExpression) expr;
        IASTExpression functionName = funCall.getFunctionNameExpression();

        if (functionName instanceof IASTIdExpression) {
          IASTName ctorName = ((IASTIdExpression) functionName).getName();
          checkIfAbstract(ctorName);
        }
      }

      return PROCESS_CONTINUE;
    }
  }

  private void checkIfAbstract(IASTDeclSpecifier declSpec) {
    IASTName className = null;

    if (declSpec instanceof ICPPASTNamedTypeSpecifier) {
      className = ((ICPPASTNamedTypeSpecifier) declSpec).getName();
    } else if (AstUtil.isClass(declSpec)) {
      className = ((ICPPASTCompositeTypeSpecifier) declSpec).getName();
    }

    if (className == null)
      return;

    IBinding binding = className.resolveBinding();

    if (binding instanceof IType) {
      reportProblemsIfAbstract((IType) binding);
    }
  }

  private void checkIfAbstract(IASTName ctorName) {
    IBinding binding = ctorName.resolveBinding();

    if (binding instanceof ICPPConstructor) {
      reportProblemsIfAbstract(((ICPPConstructor) binding).getClassOwner());
    } else if (binding instanceof IType) {
      reportProblemsIfAbstract((IType) binding);
    }
  }

  private void reportProblemsIfAbstract(IType typeToCheck) {
    IType unwindedType = CxxAstUtils.unwindTypedef(typeToCheck);

    if (!(unwindedType instanceof ICPPClassType) || unwindedType instanceof IProblemBinding)
      return;

    for (ICPPASTCompositeTypeSpecifier optClass : getClassDefinition(unwindedType)) {
      markIfHasMissingMemFuns(optClass);
    }
  }

  private Maybe<ICPPASTCompositeTypeSpecifier> getClassDefinition(IType type) {
    IType realType = AstUtil.windDownToRealType(type, false);

    if (realType instanceof ICPPClassType)
      return lookupDefinition((ICPPClassType) realType);

    return none();
  }

  private Maybe<ICPPASTCompositeTypeSpecifier> lookupDefinition(ICPPClassType type) {
    for (IASTName optClassName : findDefinitionInAst(type)) {
      ICPPASTCompositeTypeSpecifier klass = getKlassOf(optClassName);

      if (klass != null)
        return maybe(klass);
    }

    return none();
  }

  private Maybe<IASTName> findDefinitionInAst(ICPPClassType type) {
    return head(list(getAst().getDefinitionsInAST(type)));
  }

  private static ICPPASTCompositeTypeSpecifier getKlassOf(IASTNode node) {
    return AstUtil.getAncestorOfType(node, ICPPASTCompositeTypeSpecifier.class);
  }

  @Override
  protected MissingMemFunFinder getMissingMemFunsFinder() {
    return new SubtypeMissingMemFunFinder(getCProject(), getIndex());
  }

  @Override
  protected Maybe<IASTName> getNameToMark(ICPPASTCompositeTypeSpecifier klass) {
    if (klass.getName().toString().trim().isEmpty())
      // this trick is necessary because when we deal with an anonymous
      // class and we have to mark something that we can lookup afterwards
      // to find the enclosing node
      return maybe(klass.getBaseSpecifiers()[0].getName());

    return maybe(klass.getName());
  }

  @Override
  protected String getProblemId() {
    return SUBTYPE_MISSING_MEMFUNS_IMPL_PROBLEM_ID;
  }
}
