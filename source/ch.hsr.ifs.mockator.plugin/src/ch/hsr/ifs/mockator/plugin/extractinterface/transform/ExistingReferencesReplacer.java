package ch.hsr.ifs.mockator.plugin.extractinterface.transform;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.mockator.plugin.refsupport.lookup.NodeLookup;
import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;


public class ExistingReferencesReplacer implements Consumer<ExtractInterfaceContext> {

   private static final ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();

   @Override
   public void accept(final ExtractInterfaceContext context) {
      if (context.shouldReplaceAllOccurences()) {
         replaceAllOccurences(context);
      }
   }

   private static void replaceAllOccurences(final ExtractInterfaceContext context) {
      for (final IASTName usage : getUsagesOfConcreteType(context)) {
         if (isPointerOrRefOrFwdDeclToChosenClass(usage, context)) {
            replaceDeclarationWithNewType(usage, context);
         }
      }
   }

   private static Collection<IASTName> getUsagesOfConcreteType(final ExtractInterfaceContext context) {
      final IASTName chosenClassName = context.getChosenClass().getName();
      final NodeLookup lookup = new NodeLookup(context.getCProject(), context.getProgressMonitor());
      final Collection<IASTName> usages = lookup.findReferencingNames(chosenClassName, context.getCRefContext());
      addLocalUsagesIfNecessary(context, chosenClassName, usages);
      usages.addAll(lookup.findDeclarations(chosenClassName, context.getCRefContext()));
      return usages;
   }

   private static void addLocalUsagesIfNecessary(final ExtractInterfaceContext context, final IASTName className, final Collection<IASTName> usages) {
      if (!usages.isEmpty()) { return; }
      final IASTName[] references = context.getTuOfChosenClass().getReferences(className.resolveBinding());
      usages.addAll(list(references));
   }

   private static boolean isPointerOrRefOrFwdDeclToChosenClass(final IASTName usage, final ExtractInterfaceContext context) {
      if (isPartOfExpression(usage)) { return false; }
      final IASTNode declaration = getDeclaration(usage);
      final IASTDeclarator declarator = getDeclarator(declaration, context.getChosenClass().getName());
      return ASTUtil.hasPointerOrRefType(declarator) || isClassForwardDeclaration(declaration);
   }

   private static boolean isPartOfExpression(final IASTName usage) {
      return ASTUtil.getAncestorOfType(usage, ICPPASTExpression.class) != null;
   }

   private static IASTDeclarator getDeclarator(final IASTNode declaration, final IASTName className) {
      if (hasTemplateId(declaration, className)) {
         final ICPPASTNamedTypeSpecifier namedType = getTypeSpecIfRefersToClass(declaration, className);
         return ASTUtil.getChildOfType(namedType.getParent(), IASTDeclarator.class);
      }
      return ASTUtil.getDeclaratorForNode(declaration);
   }

   private static boolean hasTemplateId(final IASTNode node, final IASTName className) {
      final ICPPASTTemplateId templatedChild = ASTUtil.getChildOfType(node, ICPPASTTemplateId.class);

      if (templatedChild == null) { return false; }

      return Arrays.stream(templatedChild.getTemplateArguments()).anyMatch(t -> t instanceof ICPPASTTypeId && ((ICPPASTTypeId) t).getDeclSpecifier()
            .toString().equals(className.toString()));
   }

   private static boolean hasTemplateId(final IASTNode node) {
      return ASTUtil.getChildOfType(node, ICPPASTTemplateId.class) != null;
   }

   private static void replaceDeclarationWithNewType(final IASTName name, final ExtractInterfaceContext context) {
      final ASTRewrite rewriter = context.getRewriterFor(name.getTranslationUnit());
      final IASTNode declaration = getDeclaration(name);

      if (declaration instanceof IASTSimpleDeclaration) {
         handleSimpleDecl(context.getNewInterfaceName(), rewriter, (IASTSimpleDeclaration) declaration, name);
      } else if (declaration instanceof ICPPASTParameterDeclaration) {
         handleParameter(context.getNewInterfaceName(), rewriter, (ICPPASTParameterDeclaration) declaration, name);
      } else if (declaration instanceof ICPPASTFunctionDefinition) {
         handleReturnType(context.getNewInterfaceName(), rewriter, (ICPPASTFunctionDefinition) declaration);
      }
   }

   private static ICPPASTNamedTypeSpecifier getTypeSpecIfRefersToClass(final IASTNode selectedNode, final IASTName className) {
      if (referesToSameEntity(selectedNode, className)) { return (ICPPASTNamedTypeSpecifier) selectedNode; }

      for (final IASTNode node : selectedNode.getChildren()) {
         final ICPPASTNamedTypeSpecifier namedSpec = getTypeSpecIfRefersToClass(node, className);

         if (namedSpec != null) { return namedSpec; }
      }

      return null;
   }

   private static boolean referesToSameEntity(final IASTNode node, final IASTName astName) {
      return node instanceof ICPPASTNamedTypeSpecifier && ((ICPPASTNamedTypeSpecifier) node).getName().toString().equals(astName.toString());
   }

   private static IASTNode getDeclaration(IASTNode node) {
      while (node != null && !(node instanceof IASTSimpleDeclaration) && !(node instanceof IASTParameterDeclaration) &&
             !(node instanceof ICPPASTFunctionDefinition)) {
         node = node.getParent();
      }
      return node;
   }

   private static boolean isClassForwardDeclaration(IASTNode node) {
      if (node instanceof ICPPASTTemplateDeclaration) {
         node = ((ICPPASTTemplateDeclaration) node).getDeclaration();
      }

      if (node instanceof IASTSimpleDeclaration) { return isClassFwd(((IASTSimpleDeclaration) node).getDeclSpecifier()); }

      return false;
   }

   private static boolean isClassFwd(final IASTDeclSpecifier specifier) {
      return specifier instanceof IASTElaboratedTypeSpecifier;
   }

   private static void handleSimpleDecl(final String newInterfaceName, final ASTRewrite rewriter, final IASTSimpleDeclaration simpleDecl,
         final IASTName astName) {
      final IASTDeclSpecifier declSpec = simpleDecl.getDeclSpecifier();

      if (isClassFwd(declSpec)) {
         handleClassFwdDecl(newInterfaceName, rewriter, (ICPPASTElaboratedTypeSpecifier) declSpec);
      } else if (hasTemplateId(simpleDecl)) {
         handleTemplateId(newInterfaceName, rewriter, simpleDecl, astName);
      } else {
         handleNamedType(newInterfaceName, rewriter, declSpec);
      }
   }

   private static void handleNamedType(final String newName, final ASTRewrite r, final IASTDeclSpecifier declSpec) {
      r.replace(declSpec, createNewNamedType(newName, (ICPPASTNamedTypeSpecifier) declSpec), null);
   }

   private static void handleClassFwdDecl(final String newInterfaceName, final ASTRewrite rewriter,
         final ICPPASTElaboratedTypeSpecifier declSpecifier) {
      final ICPPASTElaboratedTypeSpecifier specifier = declSpecifier.copy();
      specifier.setName(createNewInterfaceName(newInterfaceName, specifier.getName()));
      rewriter.replace(declSpecifier, specifier, null);
   }

   private static void handleReturnType(final String interfaceName, final ASTRewrite rewriter, final ICPPASTFunctionDefinition funDef) {
      final IASTDeclSpecifier declSpec = funDef.getDeclSpecifier();
      final ICPPASTNamedTypeSpecifier newSpecifier = createNewNamedType(interfaceName, (ICPPASTNamedTypeSpecifier) declSpec);
      rewriter.replace(declSpec, newSpecifier, null);
   }

   private static void handleParameter(final String newInterfaceName, final ASTRewrite rewriter, final ICPPASTParameterDeclaration paramDecl,
         final IASTName astName) {
      if (hasTemplateId(paramDecl)) {
         handleTemplateId(newInterfaceName, rewriter, paramDecl, astName);
      } else {
         final ICPPASTNamedTypeSpecifier newSpecifier = createNewNamedType(newInterfaceName, (ICPPASTNamedTypeSpecifier) paramDecl
               .getDeclSpecifier());
         rewriter.replace(paramDecl.getDeclSpecifier(), newSpecifier, null);
      }
   }

   private static void handleTemplateId(final String newName, final ASTRewrite rewriter, final IASTNode parentDecl, final IASTName astName) {
      final ICPPASTTemplateId templateId = ASTUtil.getChildOfType(parentDecl, ICPPASTTemplateId.class);
      final IASTName templateName = templateId.getTemplateName().copy();
      final ICPPASTTemplateId newTemplateId = nodeFactory.newTemplateId(templateName);

      for (final IASTNode arg : templateId.getTemplateArguments()) {
         if (arg instanceof ICPPASTTypeId) {
            newTemplateId.addTemplateArgument(getType(newName, astName, arg));
         } else if (arg instanceof IASTExpression) {
            newTemplateId.addTemplateArgument((IASTExpression) arg.copy());
         }
      }

      rewriter.replace(templateId, newTemplateId, null);
   }

   private static ICPPASTTypeId getType(final String newInterfaceName, final IASTName astName, final IASTNode templateArg) {
      final ICPPASTTypeId copy = (ICPPASTTypeId) templateArg.copy();
      final ICPPASTNamedTypeSpecifier classSpec = getTypeSpecIfRefersToClass(templateArg, astName);

      if (classSpec != null) {
         final ICPPASTNamedTypeSpecifier newTypeSpec = createNewNamedType(newInterfaceName, classSpec);
         copy.setDeclSpecifier(newTypeSpec);
      }

      return copy;
   }

   private static ICPPASTNamedTypeSpecifier createNewNamedType(final String newInterfaceName, final ICPPASTDeclSpecifier declSpec) {
      final ICPPASTNamedTypeSpecifier newNameSpec = (ICPPASTNamedTypeSpecifier) declSpec.copy();
      final IASTName name = createNewInterfaceName(newInterfaceName, newNameSpec.getName());
      newNameSpec.setName(name);
      return newNameSpec;
   }

   private static IASTName createNewInterfaceName(final String newInterfaceName, final IASTName oldName) {
      if (ASTUtil.isQualifiedName(oldName)) { return getNewQNameForInterface((ICPPASTQualifiedName) oldName, newInterfaceName); }

      return nodeFactory.newName(newInterfaceName.toCharArray());
   }

   private static ICPPASTQualifiedName getNewQNameForInterface(final ICPPASTQualifiedName qName, final String newInterfaceName) {
      final ICPPASTQualifiedName newQfName = nodeFactory.newQualifiedName(nodeFactory.newName(newInterfaceName.toCharArray()));
      final List<ICPPASTNameSpecifier> qNameWithoutClassName = list(qName.getQualifier());

      for (final ICPPASTNameSpecifier name : qNameWithoutClassName) {
         newQfName.addNameSpecifier(name);
      }

      return newQfName;
   }
}
