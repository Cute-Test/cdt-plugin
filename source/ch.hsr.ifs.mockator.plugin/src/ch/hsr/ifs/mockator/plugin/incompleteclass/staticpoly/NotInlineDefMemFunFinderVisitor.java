package ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;

import java.util.Collection;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.QualifiedNameCreator;


@SuppressWarnings("restriction")
class NotInlineDefMemFunFinderVisitor extends ASTVisitor {

   private static final CPPNodeFactory           nodeFactory = CPPNodeFactory.getDefault();
   private final Set<ICPPASTTemplateDeclaration> templateMemFuns;
   private final ICPPASTTemplateDeclaration      templateClass;
   private final String                          fqClassName;

   {
      shouldVisitDeclarations = true;
   }

   public NotInlineDefMemFunFinderVisitor(ICPPASTTemplateDeclaration templateClass) {
      this.templateClass = templateClass;
      templateMemFuns = orderPreservingSet();
      fqClassName = getQualifiedClassName();
   }

   private String getQualifiedClassName() {
      IASTName name = getClassInTemplateDecl().getName();
      QualifiedNameCreator creator = new QualifiedNameCreator(name);
      ICPPASTQualifiedName qName = creator.createQualifiedName();
      return qName.toString();
   }

   private ICPPASTCompositeTypeSpecifier getClassInTemplateDecl() {
      return AstUtil.getChildOfType(templateClass, ICPPASTCompositeTypeSpecifier.class);
   }

   public Collection<ICPPASTTemplateDeclaration> getTemplateFunctions() {
      return templateMemFuns;
   }

   @Override
   public int visit(IASTDeclaration declaration) {
      if (!(declaration instanceof ICPPASTFunctionDefinition)) return PROCESS_CONTINUE;

      ICPPASTFunctionDefinition function = (ICPPASTFunctionDefinition) declaration;
      ICPPASTTemplateDeclaration templateDecl = getTemplateDecl(function);
      IASTName funName = function.getDeclarator().getName();

      if (templateDecl == null || !isFunNameQualified(funName)) return PROCESS_CONTINUE;

      if (isTemplateFunClassMember(funName) && haveEqualNumOfArgs(templateDecl)) {
         templateMemFuns.add(templateDecl);
         return PROCESS_SKIP;
      }

      return PROCESS_CONTINUE;
   }

   private boolean isTemplateFunClassMember(IASTName funName) {
      ICPPASTQualifiedName funNameWithoutTemplateId = getFunNameWithoutTemplateId((ICPPASTQualifiedName) funName);
      return funNameWithoutTemplateId.toString().equals(fqClassName);
   }

   private static boolean isFunNameQualified(IASTName funName) {
      return funName instanceof ICPPASTQualifiedName;
   }

   private static ICPPASTQualifiedName getFunNameWithoutTemplateId(ICPPASTQualifiedName funName) {
      ICPPASTQualifiedName qfNameWithoutTemplateId = nodeFactory.newQualifiedName();
      ICPPASTNameSpecifier[] names = funName.getAllSegments();
      for (int i = 0; i < names.length - 1; i++) {
         if (names[i] instanceof ICPPASTTemplateId) {
            qfNameWithoutTemplateId.addName(((ICPPASTTemplateId) names[i]).getTemplateName().copy());
         } else {
            qfNameWithoutTemplateId.addNameSpecifier(names[i].copy());
         }
      }
      return qfNameWithoutTemplateId;
   }

   private static ICPPASTTemplateDeclaration getTemplateDecl(ICPPASTFunctionDefinition function) {
      return AstUtil.getAncestorOfType(function, ICPPASTTemplateDeclaration.class);
   }

   private boolean haveEqualNumOfArgs(ICPPASTTemplateDeclaration templateDecl) {
      return templateDecl.getTemplateParameters().length == templateClass.getTemplateParameters().length;
   }
}
