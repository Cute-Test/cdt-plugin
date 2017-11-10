package ch.hsr.ifs.mockator.plugin.extractinterface.transform;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.core.resources.IFile;

import ch.hsr.ifs.iltis.core.resources.FileUtil;
import ch.hsr.ifs.iltis.cpp.resources.CFileUtil;

import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.AstIncludeNode;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.IncludeGuardCreator;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.NamespaceApplier;


@SuppressWarnings("restriction")
public class InterfaceClassCreator implements Consumer<ExtractInterfaceContext> {

   private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();

   @Override
   public void accept(final ExtractInterfaceContext context) {
      final ICPPASTCompositeTypeSpecifier interfaceClass = createNewInterfaceClass(context);
      addVirtualDtorTo(interfaceClass);
      addPureVirtualMemFuns(context.getChosenMemFuns(), interfaceClass, context.getChosenClass());
      final IASTNode result = createInterfaceDeclWithNamespaces(interfaceClass, context.getChosenClass());
      insertWithIncludeGuards(result, context);
   }

   private static ICPPASTCompositeTypeSpecifier createNewInterfaceClass(final ExtractInterfaceContext c) {
      final IASTName className = nodeFactory.newName(c.getNewInterfaceName().toCharArray());
      return nodeFactory.newCompositeTypeSpecifier(IASTCompositeTypeSpecifier.k_struct, className);
   }

   private static void addVirtualDtorTo(final ICPPASTCompositeTypeSpecifier newClass) {
      final IASTName dtorName = nodeFactory.newName(("~" + newClass.getName().toString()).toCharArray());
      final ICPPASTFunctionDeclarator decl = nodeFactory.newFunctionDeclarator(dtorName);
      final ICPPASTSimpleDeclSpecifier spec = nodeFactory.newSimpleDeclSpecifier();
      spec.setType(IASTSimpleDeclSpecifier.t_unspecified);
      spec.setVirtual(true);
      final IASTCompoundStatement emptyStmt = nodeFactory.newCompoundStatement();
      final ICPPASTFunctionDefinition dtor = nodeFactory.newFunctionDefinition(spec, decl, emptyStmt);
      newClass.addMemberDeclaration(dtor);
   }

   private static void addPureVirtualMemFuns(final Collection<IASTDeclaration> publicMemFuns, final ICPPASTCompositeTypeSpecifier newInterface,
         final ICPPASTCompositeTypeSpecifier klass) {
      for (final IASTDeclaration memFun : publicMemFuns) {
         final ICPPASTFunctionDeclarator oldDeclarator = AstUtil.getChildOfType(memFun, ICPPASTFunctionDeclarator.class);
         final ICPPASTFunctionDeclarator newDeclarator = createNewFunDeclarator(oldDeclarator, newInterface.getName().toString(), klass);
         final ICPPASTDeclSpecifier newDeclSpec = createNewFunDeclSpecifier(newInterface, klass, oldDeclarator, memFun);
         final IASTSimpleDeclaration funDeclaration = nodeFactory.newSimpleDeclaration(newDeclSpec);
         funDeclaration.addDeclarator(newDeclarator);
         newInterface.addMemberDeclaration(funDeclaration);
      }
   }

   private static ICPPASTFunctionDeclarator createNewFunDeclarator(final ICPPASTFunctionDeclarator oldDecl, final String newInterfaceName,
         final ICPPASTCompositeTypeSpecifier klass) {
      final IASTName funName = oldDecl.getName().copy();
      final ICPPASTFunctionDeclarator newDecl = nodeFactory.newFunctionDeclarator(funName);
      newDecl.setConst(oldDecl.isConst());
      copyPointers(oldDecl, newDecl);
      copyExceptionSpecifications(oldDecl, newDecl);
      adaptParametersIfNecessary(newInterfaceName, oldDecl, newDecl, klass);
      makePureVirtual(newDecl);
      return newDecl;
   }

   private static void copyPointers(final ICPPASTFunctionDeclarator oldDecl, final ICPPASTFunctionDeclarator newDecl) {
      for (final IASTPointerOperator pOp : oldDecl.getPointerOperators()) {
         newDecl.addPointerOperator(pOp.copy());
      }
   }

   private static void copyExceptionSpecifications(final ICPPASTFunctionDeclarator declarator, final ICPPASTFunctionDeclarator newDecl) {
      for (final IASTTypeId ex : declarator.getExceptionSpecification()) {
         newDecl.addExceptionSpecificationTypeId(ex.copy());
      }
   }

   private static void makePureVirtual(final ICPPASTFunctionDeclarator newDecl) {
      newDecl.setPureVirtual(true);
   }

   private static void adaptParametersIfNecessary(final String newInterfaceName, final ICPPASTFunctionDeclarator oldDecl,
         final ICPPASTFunctionDeclarator newDecl, final ICPPASTCompositeTypeSpecifier klass) {
      for (final ICPPASTParameterDeclaration param : createNewParameters(oldDecl, newInterfaceName, klass)) {
         newDecl.addParameterDeclaration(param);
      }
   }

   private static Collection<ICPPASTParameterDeclaration> createNewParameters(final ICPPASTFunctionDeclarator funDecl, final String newInterfaceName,
         final ICPPASTCompositeTypeSpecifier klass) {
      final List<ICPPASTParameterDeclaration> adaptedParams = list();

      for (final ICPPASTParameterDeclaration oldParam : funDecl.getParameters()) {
         final IASTDeclSpecifier paramDeclSpec = oldParam.getDeclSpecifier();
         final ICPPASTParameterDeclaration newParamDecl = oldParam.copy();

         if (hasPointerOrReferenceToInterface(oldParam.getDeclarator(), paramDeclSpec, klass)) {
            final ICPPASTNamedTypeSpecifier newTypeSpec = ((ICPPASTNamedTypeSpecifier) paramDeclSpec).copy();
            newTypeSpec.setName(nodeFactory.newName(newInterfaceName.toCharArray()));
            newParamDecl.setDeclSpecifier(newTypeSpec);
         }
         adaptedParams.add(newParamDecl);
      }

      return adaptedParams;
   }

   private static boolean hasPointerOrReferenceToInterface(final ICPPASTDeclarator declarator, final IASTDeclSpecifier declSpecifier,
         final ICPPASTCompositeTypeSpecifier klass) {
      if (AstUtil.hasPointerOrRefType(declarator) && declSpecifier instanceof ICPPASTNamedTypeSpecifier) {
         final String paramName = ((ICPPASTNamedTypeSpecifier) declSpecifier).getName().toString();
         return paramName.equals(klass.getName().toString());
      }
      return false;
   }

   private static ICPPASTDeclSpecifier createNewFunDeclSpecifier(final ICPPASTCompositeTypeSpecifier newInterface,
         final ICPPASTCompositeTypeSpecifier klass, final ICPPASTFunctionDeclarator declarator, final IASTDeclaration declaration) {
      final ICPPASTDeclSpecifier oldDeclSpec = AstUtil.getChildOfType(declaration, ICPPASTDeclSpecifier.class);
      final ICPPASTDeclSpecifier newDeclSpec = oldDeclSpec.copy();

      if (hasPointerOrReferenceToInterface(declarator, oldDeclSpec, klass)) {
         ((ICPPASTNamedTypeSpecifier) newDeclSpec).setName(newInterface.getName());
      }

      newDeclSpec.setInline(false);
      newDeclSpec.setVirtual(true);
      return newDeclSpec;
   }

   private static IASTNode createInterfaceDeclWithNamespaces(final ICPPASTCompositeTypeSpecifier newClass,
         final ICPPASTCompositeTypeSpecifier klass) {
      final IASTSimpleDeclaration newClassDecl = nodeFactory.newSimpleDeclaration(newClass);
      final NamespaceApplier applier = new NamespaceApplier(klass);
      return applier.packInSameNamespaces(newClassDecl);
   }

   private static void insertWithIncludeGuards(final IASTNode interfaceClass, final ExtractInterfaceContext context) {
      final IASTTranslationUnit ast = context.getTuOfInterface();
      final ASTRewrite rewriter = context.getRewriterFor(ast);
      final IncludeGuardCreator creator = new IncludeGuardCreator(getInterfaceFile(context), context.getCProject());
      insertIncludeGuardStart(creator, rewriter, ast);
      insertIncludes(context.getIncludes(), rewriter, ast);
      insertDeclarations(context.getClassFwdDecls(), rewriter, ast);
      insertDeclarations(context.getTypeDefDecls(), rewriter, ast);
      insertInterface(interfaceClass, rewriter, ast);
      insertIncludeGuardEnd(creator, rewriter, ast);
   }

   private static void insertIncludeGuardEnd(final IncludeGuardCreator creator, final ASTRewrite rewriter, final IASTTranslationUnit tu) {
      rewriter.insertBefore(tu, null, creator.createEndIf(), null);
   }

   private static void insertInterface(final IASTNode interfaceClass, final ASTRewrite rewriter, final IASTTranslationUnit tuOfInterface) {
      rewriter.insertBefore(tuOfInterface, null, interfaceClass, null);
   }

   private static IASTTranslationUnit insertIncludeGuardStart(final IncludeGuardCreator creator, final ASTRewrite rewriter,
         final IASTTranslationUnit tu) {
      rewriter.insertBefore(tu, null, creator.createIfNDef(), null);
      rewriter.insertBefore(tu, null, creator.createDefine(), null);
      return tu;
   }

   private static void insertDeclarations(final Collection<IASTSimpleDeclaration> declarations, final ASTRewrite rewriter,
         final IASTTranslationUnit tuOfInterface) {
      for (final IASTSimpleDeclaration decl : declarations) {
         rewriter.insertBefore(tuOfInterface, null, decl.copy(), null);
      }
   }

   private static void insertIncludes(final Collection<IASTPreprocessorIncludeStatement> includes, final ASTRewrite rewriter,
         final IASTTranslationUnit tuOfInterface) {
      for (final IASTPreprocessorIncludeStatement include : includes) {
         rewriter.insertBefore(tuOfInterface, null, new AstIncludeNode(include), null);
      }
   }

   private static IFile getInterfaceFile(final ExtractInterfaceContext context) {
      final String interfaceFilePath = context.getInterfaceFilePath();
      final IFile classFile = CFileUtil.getFile(context.getTuOfChosenClass());
      return FileUtil.toIFile(FileUtil.getPath(classFile).append(interfaceFilePath));
   }
}
