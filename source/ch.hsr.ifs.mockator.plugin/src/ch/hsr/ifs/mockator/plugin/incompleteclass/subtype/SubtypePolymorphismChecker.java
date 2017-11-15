package ch.hsr.ifs.mockator.plugin.incompleteclass.subtype;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.head;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Optional;

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

import ch.hsr.ifs.iltis.core.functional.OptHelper;

import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemFunFinder;
import ch.hsr.ifs.mockator.plugin.incompleteclass.checker.AbstractMissingMemFunChecker;
import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;


public class SubtypePolymorphismChecker extends AbstractMissingMemFunChecker {

   public static final String SUBTYPE_MISSING_MEMFUNS_IMPL_PROBLEM_ID = "ch.hsr.ifs.mockator.SubtypeMissingMemFunsProblem";

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
      public int visit(final IASTDeclaration declaration) {
         if (declaration instanceof IASTSimpleDeclaration) {
            final IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) declaration;
            checkForClassInstantiation(simpleDecl);
         }

         return PROCESS_CONTINUE;
      }

      private void checkForClassInstantiation(final IASTSimpleDeclaration simpleDecl) {
         final IASTDeclSpecifier declSpec = simpleDecl.getDeclSpecifier();

         if (declSpec.getStorageClass() == IASTDeclSpecifier.sc_typedef) { return; }

         for (final IASTDeclarator declarator : simpleDecl.getDeclarators()) {
            if (!ASTUtil.hasPointerOrRefType(declarator)) {
               checkIfAbstract(declSpec);
               break;
            }
         }
      }

      @Override
      public int visit(final IASTExpression expr) {
         if (expr instanceof ICPPASTNewExpression) {
            final ICPPASTNewExpression newExpression = (ICPPASTNewExpression) expr;

            if (!ASTUtil.hasPointerOrRefType(newExpression.getTypeId().getAbstractDeclarator())) {
               final IASTDeclSpecifier declSpec = newExpression.getTypeId().getDeclSpecifier();

               if (declSpec instanceof ICPPASTNamedTypeSpecifier) {
                  final IASTName constructorName = ((ICPPASTNamedTypeSpecifier) declSpec).getName();
                  checkIfAbstract(constructorName);
               }
            }
         } else if (expr instanceof ICPPASTFunctionCallExpression) {
            final ICPPASTFunctionCallExpression funCall = (ICPPASTFunctionCallExpression) expr;
            final IASTExpression functionName = funCall.getFunctionNameExpression();

            if (functionName instanceof IASTIdExpression) {
               final IASTName ctorName = ((IASTIdExpression) functionName).getName();
               checkIfAbstract(ctorName);
            }
         }

         return PROCESS_CONTINUE;
      }
   }

   private void checkIfAbstract(final IASTDeclSpecifier declSpec) {
      IASTName className = null;

      if (declSpec instanceof ICPPASTNamedTypeSpecifier) {
         className = ((ICPPASTNamedTypeSpecifier) declSpec).getName();
      } else if (ASTUtil.isClass(declSpec)) {
         className = ((ICPPASTCompositeTypeSpecifier) declSpec).getName();
      }

      if (className == null) { return; }

      final IBinding binding = className.resolveBinding();

      if (binding instanceof IType) {
         reportProblemsIfAbstract((IType) binding);
      }
   }

   private void checkIfAbstract(final IASTName ctorName) {
      final IBinding binding = ctorName.resolveBinding();

      if (binding instanceof ICPPConstructor) {
         reportProblemsIfAbstract(((ICPPConstructor) binding).getClassOwner());
      } else if (binding instanceof IType) {
         reportProblemsIfAbstract((IType) binding);
      }
   }

   private void reportProblemsIfAbstract(final IType typeToCheck) {
      final IType unwindedType = CxxAstUtils.unwindTypedef(typeToCheck);
      if (!(unwindedType instanceof ICPPClassType) || unwindedType instanceof IProblemBinding) { return; }
      getClassDefinition(unwindedType).ifPresent((clazz) -> markIfHasMissingMemFuns(clazz));
   }

   private Optional<ICPPASTCompositeTypeSpecifier> getClassDefinition(final IType type) {
      final IType realType = ASTUtil.windDownToRealType(type, false);

      if (realType instanceof ICPPClassType) { return lookupDefinition((ICPPClassType) realType); }

      return Optional.empty();
   }

   private Optional<ICPPASTCompositeTypeSpecifier> lookupDefinition(final ICPPClassType type) {
      return OptHelper.returnIfPresentElseEmpty(findDefinitionInAst(type), (className) -> Optional.ofNullable(getKlassOf(className)));
   }

   private Optional<IASTName> findDefinitionInAst(final ICPPClassType type) {
      return head(list(getAst().getDefinitionsInAST(type)));
   }

   private static ICPPASTCompositeTypeSpecifier getKlassOf(final IASTNode node) {
      return ASTUtil.getAncestorOfType(node, ICPPASTCompositeTypeSpecifier.class);
   }

   @Override
   protected MissingMemFunFinder getMissingMemFunsFinder() {
      return new SubtypeMissingMemFunFinder(getCProject(), getIndex());
   }

   @Override
   protected Optional<IASTName> getNameToMark(final ICPPASTCompositeTypeSpecifier clazz) {
      if (clazz.getName().toString().trim().isEmpty()) {
         // this trick is necessary because when we deal with an anonymous
         // class and we have to mark something that we can lookup afterwards
         // to find the enclosing node
         return Optional.of(clazz.getBaseSpecifiers()[0].getName());
      }

      return Optional.of(clazz.getName());
   }

   @Override
   protected String getProblemId() {
      return SUBTYPE_MISSING_MEMFUNS_IMPL_PROBLEM_ID;
   }
}
