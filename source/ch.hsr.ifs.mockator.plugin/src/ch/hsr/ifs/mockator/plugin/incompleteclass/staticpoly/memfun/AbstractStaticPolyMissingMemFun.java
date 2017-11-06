package ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly.memfun;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.incompleteclass.AbstractTestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.incompleteclass.StaticPolyMissingMemFun;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFunImplStrategy;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionSignatureFormatter;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.DefaultArgumentCreator;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;


@SuppressWarnings("restriction")
public abstract class AbstractStaticPolyMissingMemFun extends AbstractTestDoubleMemFun implements StaticPolyMissingMemFun {

   protected static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();
   private ICPPASTFunctionDeclarator     funDecl;

   @Override
   public String getFunctionSignature() {
      return new FunctionSignatureFormatter(getFunDecl()).getFunctionSignature();
   }

   @Override
   public ICPPASTFunctionDefinition createFunctionDefinition(final TestDoubleMemFunImplStrategy strategy, final CppStandard cppStd) {
      final ICPPASTFunctionDeclarator funDecl = createFunDecl();
      final ICPPASTDeclSpecifier returnType = createReturnType(funDecl);
      final IASTCompoundStatement funBody = createFunBody(strategy, funDecl, returnType, cppStd);
      return nodeFactory.newFunctionDefinition(returnType, funDecl, funBody);
   }

   private ICPPASTFunctionDeclarator getFunDecl() {
      if (funDecl == null) {
         funDecl = createFunDecl();
      }
      return funDecl;
   }

   protected abstract ICPPASTFunctionDeclarator createFunDecl();

   protected abstract ICPPASTDeclSpecifier createReturnType(ICPPASTFunctionDeclarator funDecl);

   protected abstract IASTCompoundStatement createFunBody(TestDoubleMemFunImplStrategy strategy, ICPPASTFunctionDeclarator funDecl,
         ICPPASTDeclSpecifier specifier, CppStandard cppStd);

   protected IASTCompoundStatement createEmptyFunBody() {
      return nodeFactory.newCompoundStatement();
   }

   @Override
   public ICPPASTFunctionDefinition getContainingFunction() {
      final IASTExpression expr = getUnderlyingExpression();
      return AstUtil.getAncestorOfType(expr, ICPPASTFunctionDefinition.class);
   }

   protected abstract IASTExpression getUnderlyingExpression();

   @Override
   public Collection<IASTInitializerClause> createDefaultArguments(final CppStandard cppStd, final LinkedEditModeStrategy linkedEditStrategy) {
      final DefaultArgumentCreator creator = new DefaultArgumentCreator(linkedEditStrategy, cppStd);
      return creator.createDefaultArguments(list(getFunDecl().getParameters()));
   }

   protected ICPPASTDeclSpecifier createCtorReturnType() {
      return nodeFactory.newSimpleDeclSpecifier();
   }
}
