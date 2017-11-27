package ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.NullProgressMonitor;

import ch.hsr.ifs.iltis.core.data.AbstractPair;
import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.wrappers.CPPVisitor;
import ch.hsr.ifs.mockator.plugin.refsupport.lookup.NodeLookup;


class ClassInTemplateIdFinderVisitor extends ASTVisitor {

   private final ICPPASTCompositeTypeSpecifier testDouble;
   private final Set<TemplateParamCombination> templateParams;
   private final ICProject                     cProject;
   private final IIndex                        index;

   {
      shouldVisitNames = true;
   }

   public ClassInTemplateIdFinderVisitor(final ICPPASTCompositeTypeSpecifier testDouble, final ICProject cProject, final IIndex index) {
      this.testDouble = testDouble;
      this.cProject = cProject;
      this.index = index;
      templateParams = orderPreservingSet();
   }

   public Collection<TemplateParamCombination> getTemplateParamCombinations() {
      return templateParams;
   }

   @Override
   public int visit(final IASTName name) {
      final IBinding binding = name.resolveBinding();

      if (!(binding instanceof ICPPTemplateInstance)) {
         return PROCESS_CONTINUE;
      }

      processTemplateArguments((ICPPTemplateInstance) binding, name);
      return PROCESS_SKIP;
   }

   private void processTemplateArguments(final ICPPTemplateInstance templateInstance, final IASTName name) {
      final IType testDoubleType = CPPVisitor.createType(testDouble);
      final ICPPTemplateArgument[] templateArguments = templateInstance.getTemplateArguments();
      final List<Integer> positions = list();

      for (int i = 0; i < templateArguments.length; i++) {
         final IType templateArg = templateArguments[i].getTypeValue();

         if (ASTUtil.isSameType(templateArg, testDoubleType) || refersToTestDouble(name, i)) {
            positions.add(i);
         }
      }

      if (positions.isEmpty()) {
         return;
      }

      processTemplateDefinition(positions, templateInstance.getTemplateDefinition());
   }

   private boolean refersToTestDouble(final IASTName name, final int templateArgPos) {
      if (!(name instanceof ICPPASTTemplateId)) {
         return false;
      }

      final IASTNode node = ((ICPPASTTemplateId) name).getTemplateArguments()[templateArgPos];

      if (node instanceof ICPPASTTypeId) {
         final IASTDeclSpecifier declSpecifier = ((ICPPASTTypeId) node).getDeclSpecifier();

         if (declSpecifier instanceof ICPPASTNamedTypeSpecifier) {
            final IASTName argName = ((ICPPASTNamedTypeSpecifier) declSpecifier).getName();
            return argName.resolveBinding().equals(testDouble.getName().resolveBinding());
         }
      }

      return false;
   }

   private void processTemplateDefinition(final Collection<Integer> positions, final ICPPTemplateDefinition definition) {
      for (final ICPPASTTemplateDeclaration candidate : findTemplate(definition)) {
         final ICPPASTTemplateParameter[] templateParams = candidate.getTemplateParameters();

         for (final Integer pos : positions) {
            ILTISException.Unless.isTrue(pos < templateParams.length, "Wrong deduction of template parameter position");
            addToTemplateParamCombinations(candidate, templateParams[pos]);
         }
      }
   }

   private void addToTemplateParamCombinations(final ICPPASTTemplateDeclaration templateDecl, final ICPPASTTemplateParameter templateParam) {
      templateParams.add(new TemplateParamCombination(templateDecl, templateParam));
   }

   private static Collection<ICPPASTTemplateDeclaration> lookupInAst(final IASTName name) {
      final ICPPASTTemplateDeclaration templateDecl = ASTUtil.getAncestorOfType(name, ICPPASTTemplateDeclaration.class);
      return list(templateDecl);
   }

   private Collection<ICPPASTTemplateDeclaration> findTemplate(final IBinding template) {
      final IASTName[] definitionNames = testDouble.getTranslationUnit().getDefinitionsInAST(template);

      if (definitionNames.length > 0) {
         return lookupInAst(definitionNames[0]);
      }

      final List<ICPPASTTemplateDeclaration> templates = list();

      lookupInIndex(template).ifPresent((candidate) -> templates.add(candidate));
      return templates;
   }

   private Optional<ICPPASTTemplateDeclaration> lookupInIndex(final IBinding template) {
      final NodeLookup lookup = new NodeLookup(cProject, new NullProgressMonitor());
      return lookup.findTemplateDefinition(template, index);
   }

   public class TemplateParamCombination extends AbstractPair<ICPPASTTemplateDeclaration, ICPPASTTemplateParameter> {

      public TemplateParamCombination(final ICPPASTTemplateDeclaration templateDecl, final ICPPASTTemplateParameter templateParam) {
         super(templateDecl, templateParam);
      }

      public ICPPASTTemplateDeclaration decl() {
         return first;
      }

      public ICPPASTTemplateParameter param() {
         return second;
      }
   }
}
