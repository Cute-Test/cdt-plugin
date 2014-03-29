package ch.hsr.ifs.mockator.plugin.refsupport.finder;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder.filter;

import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.mockator.plugin.base.functional.F1;
import ch.hsr.ifs.mockator.plugin.project.properties.FunctionsToAnalyze;
import ch.hsr.ifs.mockator.plugin.refsupport.lookup.NodeLookup;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

@SuppressWarnings("restriction")
public class ReferencingTestFunFinder {
  private final ICProject cProject;
  private final ICPPASTCompositeTypeSpecifier testDouble;

  public ReferencingTestFunFinder(ICProject cProject, ICPPASTCompositeTypeSpecifier testDouble) {
    this.cProject = cProject;
    this.testDouble = testDouble;
  }

  public Collection<ICPPASTFunctionDefinition> findByIndexLookup(CRefactoringContext context,
      IProgressMonitor pm) {
    return filterTestFunctions(getReferencingFunctions(testDouble, context, pm));
  }

  public Collection<ICPPASTFunctionDefinition> findInAst(IASTTranslationUnit ast) {
    List<ICPPASTFunctionDefinition> functions = list();

    for (IASTName astNode : ast.getReferences(testDouble.getName().resolveBinding())) {
      ICPPASTFunctionDefinition function = getFunctionParent(astNode);

      if (function != null) {
        functions.add(function);
      }
    }

    return filterTestFunctions(functions);
  }

  public Collection<ICPPASTFunctionDefinition> filterTestFunctions(
      Collection<ICPPASTFunctionDefinition> functions) {
    List<ICPPASTFunctionDefinition> testFunctions =
        list(filter(functions, new F1<ICPPASTFunctionDefinition, Boolean>() {
          @Override
          public Boolean apply(ICPPASTFunctionDefinition function) {
            return isValidTestFunction(function);
          }
        }));
    addContainingFunctionIfNecessary(testFunctions);
    return testFunctions;
  }

  private Collection<ICPPASTFunctionDefinition> getReferencingFunctions(
      ICPPASTCompositeTypeSpecifier testDouble, CRefactoringContext context, IProgressMonitor pm) {
    NodeLookup lookup = new NodeLookup(cProject, pm);
    return lookup.findReferencingFunctions(testDouble.getName(), context);
  }

  private void addContainingFunctionIfNecessary(List<ICPPASTFunctionDefinition> testFunctions) {
    if (!testFunctions.isEmpty())
      return;

    ICPPASTFunctionDefinition testFunction = getContainingTestFunction(testDouble);

    if (testFunction != null) {
      testFunctions.add(testFunction);
    }
  }

  private ICPPASTFunctionDefinition getContainingTestFunction(
      ICPPASTCompositeTypeSpecifier testDouble) {
    ICPPASTFunctionDefinition containedFunction = getFunctionParent(testDouble);

    if (containedFunction != null && isValidTestFunction(containedFunction))
      return containedFunction;

    return null;
  }

  private boolean isValidTestFunction(ICPPASTFunctionDefinition function) {
    return FunctionsToAnalyze.fromProjectSettings(cProject.getProject()).shouldConsider(function);
  }

  private static ICPPASTFunctionDefinition getFunctionParent(IASTNode astNode) {
    return AstUtil.getAncestorOfType(astNode, ICPPASTFunctionDefinition.class);
  }
}
