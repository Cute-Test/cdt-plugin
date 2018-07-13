package ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.mockator.plugin.refsupport.utils.QualifiedNameCreator;


class NotInlineDefMemFunFinderVisitor extends ASTVisitor {

   private static final ICPPNodeFactory          nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   private final Set<ICPPASTTemplateDeclaration> templateMemFuns;
   private final ICPPASTTemplateDeclaration      templateClass;
   private final String                          fqClassName;

   {
      shouldVisitDeclarations = true;
   }

   public NotInlineDefMemFunFinderVisitor(final ICPPASTTemplateDeclaration templateClass) {
      this.templateClass = templateClass;
      templateMemFuns = new LinkedHashSet<>();
      fqClassName = getQualifiedClassName();
   }

   private String getQualifiedClassName() {
      final IASTName name = getClassInTemplateDecl().getName();
      final QualifiedNameCreator creator = new QualifiedNameCreator(name);
      final ICPPASTQualifiedName qName = creator.createQualifiedName();
      return qName.toString();
   }

   private ICPPASTCompositeTypeSpecifier getClassInTemplateDecl() {
      return CPPVisitor.findChildWithType(templateClass, ICPPASTCompositeTypeSpecifier.class).orElse(null);
   }

   public Collection<ICPPASTTemplateDeclaration> getTemplateFunctions() {
      return templateMemFuns;
   }

   @Override
   public int visit(final IASTDeclaration declaration) {
      if (!(declaration instanceof ICPPASTFunctionDefinition)) { return PROCESS_CONTINUE; }

      final ICPPASTFunctionDefinition function = (ICPPASTFunctionDefinition) declaration;
      final ICPPASTTemplateDeclaration templateDecl = getTemplateDecl(function);
      final IASTName funName = function.getDeclarator().getName();

      if (templateDecl == null || !isFunNameQualified(funName)) { return PROCESS_CONTINUE; }

      if (isTemplateFunClassMember(funName) && haveEqualNumOfArgs(templateDecl)) {
         templateMemFuns.add(templateDecl);
         return PROCESS_SKIP;
      }

      return PROCESS_CONTINUE;
   }

   private boolean isTemplateFunClassMember(final IASTName funName) {
      final ICPPASTQualifiedName funNameWithoutTemplateId = getFunNameWithoutTemplateId((ICPPASTQualifiedName) funName);
      return funNameWithoutTemplateId.toString().equals(fqClassName);
   }

   private static boolean isFunNameQualified(final IASTName funName) {
      return funName instanceof ICPPASTQualifiedName;
   }

   private static ICPPASTQualifiedName getFunNameWithoutTemplateId(final ICPPASTQualifiedName funName) {
      final ICPPASTQualifiedName qfNameWithoutTemplateId = nodeFactory.newQualifiedName(null);
      final ICPPASTNameSpecifier[] names = funName.getAllSegments();
      for (int i = 0; i < names.length - 1; i++) {
         if (names[i] instanceof ICPPASTTemplateId) {
            qfNameWithoutTemplateId.addName(((ICPPASTTemplateId) names[i]).getTemplateName().copy());
         } else {
            qfNameWithoutTemplateId.addNameSpecifier(names[i].copy());
         }
      }
      return qfNameWithoutTemplateId;
   }

   private static ICPPASTTemplateDeclaration getTemplateDecl(final ICPPASTFunctionDefinition function) {
      return CPPVisitor.findAncestorWithType(function, ICPPASTTemplateDeclaration.class).orElse(null);
   }

   private boolean haveEqualNumOfArgs(final ICPPASTTemplateDeclaration templateDecl) {
      return templateDecl.getTemplateParameters().length == templateClass.getTemplateParameters().length;
   }
}
