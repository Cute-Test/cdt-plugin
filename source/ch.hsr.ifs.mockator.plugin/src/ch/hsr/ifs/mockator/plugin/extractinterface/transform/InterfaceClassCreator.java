package ch.hsr.ifs.mockator.plugin.extractinterface.transform;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;
import java.util.List;

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

import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.base.util.FileUtil;
import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.AstIncludeNode;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.IncludeGuardCreator;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.NamespaceApplier;


@SuppressWarnings("restriction")
public class InterfaceClassCreator implements F1V<ExtractInterfaceContext> {

   private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();

   @Override
   public void apply(ExtractInterfaceContext context) {
      ICPPASTCompositeTypeSpecifier interfaceClass = createNewInterfaceClass(context);
      addVirtualDtorTo(interfaceClass);
      addPureVirtualMemFuns(context.getChosenMemFuns(), interfaceClass, context.getChosenClass());
      IASTNode result = createInterfaceDeclWithNamespaces(interfaceClass, context.getChosenClass());
      insertWithIncludeGuards(result, context);
   }

   private static ICPPASTCompositeTypeSpecifier createNewInterfaceClass(ExtractInterfaceContext c) {
      IASTName className = nodeFactory.newName(c.getNewInterfaceName().toCharArray());
      return nodeFactory.newCompositeTypeSpecifier(IASTCompositeTypeSpecifier.k_struct, className);
   }

   private static void addVirtualDtorTo(ICPPASTCompositeTypeSpecifier newClass) {
      IASTName dtorName = nodeFactory.newName(("~" + newClass.getName().toString()).toCharArray());
      ICPPASTFunctionDeclarator decl = nodeFactory.newFunctionDeclarator(dtorName);
      ICPPASTSimpleDeclSpecifier spec = nodeFactory.newSimpleDeclSpecifier();
      spec.setType(IASTSimpleDeclSpecifier.t_unspecified);
      spec.setVirtual(true);
      IASTCompoundStatement emptyStmt = nodeFactory.newCompoundStatement();
      ICPPASTFunctionDefinition dtor = nodeFactory.newFunctionDefinition(spec, decl, emptyStmt);
      newClass.addMemberDeclaration(dtor);
   }

   private static void addPureVirtualMemFuns(Collection<IASTDeclaration> publicMemFuns, ICPPASTCompositeTypeSpecifier newInterface,
         ICPPASTCompositeTypeSpecifier klass) {
      for (IASTDeclaration memFun : publicMemFuns) {
         ICPPASTFunctionDeclarator oldDeclarator = AstUtil.getChildOfType(memFun, ICPPASTFunctionDeclarator.class);
         ICPPASTFunctionDeclarator newDeclarator = createNewFunDeclarator(oldDeclarator, newInterface.getName().toString(), klass);
         ICPPASTDeclSpecifier newDeclSpec = createNewFunDeclSpecifier(newInterface, klass, oldDeclarator, memFun);
         IASTSimpleDeclaration funDeclaration = nodeFactory.newSimpleDeclaration(newDeclSpec);
         funDeclaration.addDeclarator(newDeclarator);
         newInterface.addMemberDeclaration(funDeclaration);
      }
   }

   private static ICPPASTFunctionDeclarator createNewFunDeclarator(ICPPASTFunctionDeclarator oldDecl, String newInterfaceName,
         ICPPASTCompositeTypeSpecifier klass) {
      IASTName funName = oldDecl.getName().copy();
      ICPPASTFunctionDeclarator newDecl = nodeFactory.newFunctionDeclarator(funName);
      newDecl.setConst(oldDecl.isConst());
      copyPointers(oldDecl, newDecl);
      copyExceptionSpecifications(oldDecl, newDecl);
      adaptParametersIfNecessary(newInterfaceName, oldDecl, newDecl, klass);
      makePureVirtual(newDecl);
      return newDecl;
   }

   private static void copyPointers(ICPPASTFunctionDeclarator oldDecl, ICPPASTFunctionDeclarator newDecl) {
      for (IASTPointerOperator pOp : oldDecl.getPointerOperators()) {
         newDecl.addPointerOperator(pOp.copy());
      }
   }

   private static void copyExceptionSpecifications(ICPPASTFunctionDeclarator declarator, ICPPASTFunctionDeclarator newDecl) {
      for (IASTTypeId ex : declarator.getExceptionSpecification()) {
         newDecl.addExceptionSpecificationTypeId(ex.copy());
      }
   }

   private static void makePureVirtual(ICPPASTFunctionDeclarator newDecl) {
      newDecl.setPureVirtual(true);
   }

   private static void adaptParametersIfNecessary(String newInterfaceName, ICPPASTFunctionDeclarator oldDecl, ICPPASTFunctionDeclarator newDecl,
         ICPPASTCompositeTypeSpecifier klass) {
      for (ICPPASTParameterDeclaration param : createNewParameters(oldDecl, newInterfaceName, klass)) {
         newDecl.addParameterDeclaration(param);
      }
   }

   private static Collection<ICPPASTParameterDeclaration> createNewParameters(ICPPASTFunctionDeclarator funDecl, String newInterfaceName,
         ICPPASTCompositeTypeSpecifier klass) {
      List<ICPPASTParameterDeclaration> adaptedParams = list();

      for (ICPPASTParameterDeclaration oldParam : funDecl.getParameters()) {
         IASTDeclSpecifier paramDeclSpec = oldParam.getDeclSpecifier();
         ICPPASTParameterDeclaration newParamDecl = oldParam.copy();

         if (hasPointerOrReferenceToInterface(oldParam.getDeclarator(), paramDeclSpec, klass)) {
            ICPPASTNamedTypeSpecifier newTypeSpec = ((ICPPASTNamedTypeSpecifier) paramDeclSpec).copy();
            newTypeSpec.setName(nodeFactory.newName(newInterfaceName.toCharArray()));
            newParamDecl.setDeclSpecifier(newTypeSpec);
         }
         adaptedParams.add(newParamDecl);
      }

      return adaptedParams;
   }

   private static boolean hasPointerOrReferenceToInterface(ICPPASTDeclarator declarator, IASTDeclSpecifier declSpecifier,
         ICPPASTCompositeTypeSpecifier klass) {
      if (AstUtil.hasPointerOrRefType(declarator) && declSpecifier instanceof ICPPASTNamedTypeSpecifier) {
         String paramName = ((ICPPASTNamedTypeSpecifier) declSpecifier).getName().toString();
         return paramName.equals(klass.getName().toString());
      }
      return false;
   }

   private static ICPPASTDeclSpecifier createNewFunDeclSpecifier(ICPPASTCompositeTypeSpecifier newInterface, ICPPASTCompositeTypeSpecifier klass,
         ICPPASTFunctionDeclarator declarator, IASTDeclaration declaration) {
      ICPPASTDeclSpecifier oldDeclSpec = AstUtil.getChildOfType(declaration, ICPPASTDeclSpecifier.class);
      ICPPASTDeclSpecifier newDeclSpec = oldDeclSpec.copy();

      if (hasPointerOrReferenceToInterface(declarator, oldDeclSpec, klass)) {
         ((ICPPASTNamedTypeSpecifier) newDeclSpec).setName(newInterface.getName());
      }

      newDeclSpec.setInline(false);
      newDeclSpec.setVirtual(true);
      return newDeclSpec;
   }

   private static IASTNode createInterfaceDeclWithNamespaces(ICPPASTCompositeTypeSpecifier newClass, ICPPASTCompositeTypeSpecifier klass) {
      IASTSimpleDeclaration newClassDecl = nodeFactory.newSimpleDeclaration(newClass);
      NamespaceApplier applier = new NamespaceApplier(klass);
      return applier.packInSameNamespaces(newClassDecl);
   }

   private static void insertWithIncludeGuards(IASTNode interfaceClass, ExtractInterfaceContext context) {
      IASTTranslationUnit ast = context.getTuOfInterface();
      ASTRewrite rewriter = context.getRewriterFor(ast);
      IncludeGuardCreator creator = new IncludeGuardCreator(getInterfaceFile(context), context.getCProject());
      insertIncludeGuardStart(creator, rewriter, ast);
      insertIncludes(context.getIncludes(), rewriter, ast);
      insertDeclarations(context.getClassFwdDecls(), rewriter, ast);
      insertDeclarations(context.getTypeDefDecls(), rewriter, ast);
      insertInterface(interfaceClass, rewriter, ast);
      insertIncludeGuardEnd(creator, rewriter, ast);
   }

   private static void insertIncludeGuardEnd(IncludeGuardCreator creator, ASTRewrite rewriter, IASTTranslationUnit tu) {
      rewriter.insertBefore(tu, null, creator.createEndIf(), null);
   }

   private static void insertInterface(IASTNode interfaceClass, ASTRewrite rewriter, IASTTranslationUnit tuOfInterface) {
      rewriter.insertBefore(tuOfInterface, null, interfaceClass, null);
   }

   private static IASTTranslationUnit insertIncludeGuardStart(IncludeGuardCreator creator, ASTRewrite rewriter, IASTTranslationUnit tu) {
      rewriter.insertBefore(tu, null, creator.createIfNDef(), null);
      rewriter.insertBefore(tu, null, creator.createDefine(), null);
      return tu;
   }

   private static void insertDeclarations(Collection<IASTSimpleDeclaration> declarations, ASTRewrite rewriter, IASTTranslationUnit tuOfInterface) {
      for (IASTSimpleDeclaration decl : declarations) {
         rewriter.insertBefore(tuOfInterface, null, decl.copy(), null);
      }
   }

   private static void insertIncludes(Collection<IASTPreprocessorIncludeStatement> includes, ASTRewrite rewriter, IASTTranslationUnit tuOfInterface) {
      for (IASTPreprocessorIncludeStatement include : includes) {
         rewriter.insertBefore(tuOfInterface, null, new AstIncludeNode(include), null);
      }
   }

   private static IFile getInterfaceFile(ExtractInterfaceContext context) {
      String interfaceFilePath = context.getInterfaceFilePath();
      IFile classFile = FileUtil.getFile(context.getTuOfChosenClass());
      return FileUtil.toIFile(FileUtil.getPath(classFile).append(interfaceFilePath));
   }
}
