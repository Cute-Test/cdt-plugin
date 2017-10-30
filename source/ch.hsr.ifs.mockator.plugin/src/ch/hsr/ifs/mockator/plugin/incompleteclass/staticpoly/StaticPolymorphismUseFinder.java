package ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;
import static ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder.filter;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._1;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._2;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;

import ch.hsr.ifs.mockator.plugin.base.functional.F1;
import ch.hsr.ifs.mockator.plugin.base.tuples.Pair;
import ch.hsr.ifs.mockator.plugin.incompleteclass.StaticPolyMissingMemFun;
import ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly.memfun.MissingMemFunCollector;
import ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly.referenced.NotReferencedFunctionFilter;
import ch.hsr.ifs.mockator.plugin.project.properties.MarkMissingMemFuns;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;


class StaticPolymorphismUseFinder implements F1<IASTFunctionDefinition, Collection<StaticPolyMissingMemFun>> {

   private final ICProject                     cProject;
   private final ICPPASTCompositeTypeSpecifier testDouble;
   private final IIndex                        index;

   public StaticPolymorphismUseFinder(ICPPASTCompositeTypeSpecifier testDouble, ICProject cProject, IIndex index) {
      this.testDouble = testDouble;
      this.cProject = cProject;
      this.index = index;
   }

   @Override
   public Collection<StaticPolyMissingMemFun> apply(IASTFunctionDefinition testFunction) {
      Collection<StaticPolyMissingMemFun> missingMemFuns = orderPreservingSet();

      for (Pair<ICPPASTTemplateDeclaration, ICPPASTTemplateParameter> declParam : getTestDoubleAsTemplateArgUsages(testFunction)) {
         missingMemFuns.addAll(collectMissingMemFuns(testFunction, declParam));
      }

      return missingMemFuns;
   }

   private Collection<StaticPolyMissingMemFun> collectMissingMemFuns(IASTFunctionDefinition testFun,
         Pair<ICPPASTTemplateDeclaration, ICPPASTTemplateParameter> declParam) {
      Collection<StaticPolyMissingMemFun> missingFuns = collectUsedMemFunsInSut(declParam);
      if (considerOnlyReferencedMemFuns()) {
         missingFuns = filterNotReferenced(missingFuns, _1(declParam).getTranslationUnit(), testFun);
      }
      return missingFuns;
   }

   private boolean considerOnlyReferencedMemFuns() {
      return MarkMissingMemFuns.fromProjectSettings(cProject.getProject()) == MarkMissingMemFuns.OnlyReferencedFromTest;
   }

   private Collection<StaticPolyMissingMemFun> collectUsedMemFunsInSut(Pair<ICPPASTTemplateDeclaration, ICPPASTTemplateParameter> declParam) {
      Collection<ICPPASTTemplateDeclaration> funs = getTemplateFunctions(_1(declParam));
      MissingMemFunCollector finder = new MissingMemFunCollector(_1(declParam), testDouble, funs);
      return finder.getMissingMemberFunctions(_2(declParam));
   }

   private static Collection<ICPPASTTemplateDeclaration> getTemplateFunctions(ICPPASTTemplateDeclaration templateDecl) {
      if (!hasClassInTemplateDecl(templateDecl)) return list();

      NotInlineDefMemFunFinderVisitor visitor = new NotInlineDefMemFunFinderVisitor(templateDecl);
      templateDecl.getTranslationUnit().accept(visitor);
      return visitor.getTemplateFunctions();
   }

   private static boolean hasClassInTemplateDecl(ICPPASTTemplateDeclaration templateDecl) {
      return AstUtil.getChildOfType(templateDecl, ICPPASTCompositeTypeSpecifier.class) != null;
   }

   private Collection<Pair<ICPPASTTemplateDeclaration, ICPPASTTemplateParameter>> getTestDoubleAsTemplateArgUsages(
         IASTFunctionDefinition testFunction) {
      ClassInTemplateIdFinderVisitor finder = new ClassInTemplateIdFinderVisitor(testDouble, cProject, index);
      testFunction.accept(finder);
      return finder.getTemplateParamCombinations();
   }

   private Collection<StaticPolyMissingMemFun> filterNotReferenced(Collection<StaticPolyMissingMemFun> missingMemFuns,
         IASTTranslationUnit tuOfTemplate, IASTFunctionDefinition testFunction) {
      NotReferencedFunctionFilter filter = getNotReferencedFunFilter(tuOfTemplate, testFunction);
      return filter(missingMemFuns, filter);
   }

   private NotReferencedFunctionFilter getNotReferencedFunFilter(IASTTranslationUnit tuOfTemplate, IASTFunctionDefinition testFunction) {
      return new NotReferencedFunctionFilter(index, cProject, (ICPPASTFunctionDefinition) testFunction);
   }
}
