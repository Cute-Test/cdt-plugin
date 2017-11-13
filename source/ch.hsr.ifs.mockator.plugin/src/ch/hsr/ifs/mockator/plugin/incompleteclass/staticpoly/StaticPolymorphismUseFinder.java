package ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;

import ch.hsr.ifs.mockator.plugin.base.data.Pair;
import ch.hsr.ifs.mockator.plugin.incompleteclass.StaticPolyMissingMemFun;
import ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly.memfun.MissingMemFunCollector;
import ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly.referenced.NotReferencedFunctionFilter;
import ch.hsr.ifs.mockator.plugin.project.properties.MarkMissingMemFuns;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;


class StaticPolymorphismUseFinder implements Function<IASTFunctionDefinition, Collection<StaticPolyMissingMemFun>> {

   private final ICProject                     cProject;
   private final ICPPASTCompositeTypeSpecifier testDouble;
   private final IIndex                        index;

   public StaticPolymorphismUseFinder(final ICPPASTCompositeTypeSpecifier testDouble, final ICProject cProject, final IIndex index) {
      this.testDouble = testDouble;
      this.cProject = cProject;
      this.index = index;
   }

   @Override
   public Collection<StaticPolyMissingMemFun> apply(final IASTFunctionDefinition testFunction) {
      final Collection<StaticPolyMissingMemFun> missingMemFuns = orderPreservingSet();

      for (final Pair<ICPPASTTemplateDeclaration, ICPPASTTemplateParameter> declParam : getTestDoubleAsTemplateArgUsages(testFunction)) {
         missingMemFuns.addAll(collectMissingMemFuns(testFunction, declParam));
      }

      return missingMemFuns;
   }

   private Collection<StaticPolyMissingMemFun> collectMissingMemFuns(final IASTFunctionDefinition testFun,
         final Pair<ICPPASTTemplateDeclaration, ICPPASTTemplateParameter> declParam) {
      Collection<StaticPolyMissingMemFun> missingFuns = collectUsedMemFunsInSut(declParam);
      if (considerOnlyReferencedMemFuns()) {
         missingFuns = filterNotReferenced(missingFuns, declParam.first().getTranslationUnit(), testFun);
      }
      return missingFuns;
   }

   private boolean considerOnlyReferencedMemFuns() {
      return MarkMissingMemFuns.fromProjectSettings(cProject.getProject()) == MarkMissingMemFuns.OnlyReferencedFromTest;
   }

   private Collection<StaticPolyMissingMemFun> collectUsedMemFunsInSut(final Pair<ICPPASTTemplateDeclaration, ICPPASTTemplateParameter> declParam) {
      final Collection<ICPPASTTemplateDeclaration> funs = getTemplateFunctions(declParam.first());
      final MissingMemFunCollector finder = new MissingMemFunCollector(declParam.first(), testDouble, funs);
      return finder.getMissingMemberFunctions(declParam.second());
   }

   private static Collection<ICPPASTTemplateDeclaration> getTemplateFunctions(final ICPPASTTemplateDeclaration templateDecl) {
      if (!hasClassInTemplateDecl(templateDecl)) {
         return list();
      }

      final NotInlineDefMemFunFinderVisitor visitor = new NotInlineDefMemFunFinderVisitor(templateDecl);
      templateDecl.getTranslationUnit().accept(visitor);
      return visitor.getTemplateFunctions();
   }

   private static boolean hasClassInTemplateDecl(final ICPPASTTemplateDeclaration templateDecl) {
      return AstUtil.getChildOfType(templateDecl, ICPPASTCompositeTypeSpecifier.class) != null;
   }

   private Collection<Pair<ICPPASTTemplateDeclaration, ICPPASTTemplateParameter>> getTestDoubleAsTemplateArgUsages(
         final IASTFunctionDefinition testFunction) {
      final ClassInTemplateIdFinderVisitor finder = new ClassInTemplateIdFinderVisitor(testDouble, cProject, index);
      testFunction.accept(finder);
      return finder.getTemplateParamCombinations();
   }

   private Collection<StaticPolyMissingMemFun> filterNotReferenced(final Collection<StaticPolyMissingMemFun> missingMemFuns,
         final IASTTranslationUnit tuOfTemplate, final IASTFunctionDefinition testFunction) {
      final NotReferencedFunctionFilter filter = getNotReferencedFunFilter(tuOfTemplate, testFunction);
      return missingMemFuns.stream().filter(filter).collect(Collectors.toList());
   }

   private NotReferencedFunctionFilter getNotReferencedFunFilter(final IASTTranslationUnit tuOfTemplate, final IASTFunctionDefinition testFunction) {
      return new NotReferencedFunctionFilter(index, cProject, (ICPPASTFunctionDefinition) testFunction);
   }
}
