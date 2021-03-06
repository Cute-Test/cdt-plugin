package ch.hsr.ifs.cute.mockator.refsupport.finder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CRefactoringContext;

import ch.hsr.ifs.cute.mockator.project.properties.FunctionsToAnalyze;
import ch.hsr.ifs.cute.mockator.refsupport.lookup.NodeLookup;


public class ReferencingTestFunFinder {

    private final ICProject                     cProject;
    private final ICPPASTCompositeTypeSpecifier testDouble;

    public ReferencingTestFunFinder(final ICProject cProject, final ICPPASTCompositeTypeSpecifier testDouble) {
        this.cProject = cProject;
        this.testDouble = testDouble;
    }

    public Collection<ICPPASTFunctionDefinition> findByIndexLookup(final CRefactoringContext context, final IProgressMonitor pm) {
        return filterTestFunctions(getReferencingFunctions(testDouble, context, pm));
    }

    public Collection<ICPPASTFunctionDefinition> findInAst(final IASTTranslationUnit ast) {
        final List<ICPPASTFunctionDefinition> functions = new ArrayList<>();

        for (final IASTName astNode : ast.getReferences(testDouble.getName().resolveBinding())) {
            final ICPPASTFunctionDefinition function = getFunctionParent(astNode);

            if (function != null) {
                functions.add(function);
            }
        }

        return filterTestFunctions(functions);
    }

    public Collection<ICPPASTFunctionDefinition> filterTestFunctions(final Collection<ICPPASTFunctionDefinition> functions) {
        final List<ICPPASTFunctionDefinition> testFunctions = functions.stream().filter((function) -> isValidTestFunction(function)).collect(
                Collectors.toList());
        addContainingFunctionIfNecessary(testFunctions);
        return testFunctions;
    }

    private Collection<ICPPASTFunctionDefinition> getReferencingFunctions(final ICPPASTCompositeTypeSpecifier testDouble,
            final CRefactoringContext context, final IProgressMonitor pm) {
        final NodeLookup lookup = new NodeLookup(cProject, pm);
        return lookup.findReferencingFunctions(testDouble.getName(), context);
    }

    private void addContainingFunctionIfNecessary(final List<ICPPASTFunctionDefinition> testFunctions) {
        if (!testFunctions.isEmpty()) {
            return;
        }

        final ICPPASTFunctionDefinition testFunction = getContainingTestFunction(testDouble);

        if (testFunction != null) {
            testFunctions.add(testFunction);
        }
    }

    private ICPPASTFunctionDefinition getContainingTestFunction(final ICPPASTCompositeTypeSpecifier testDouble) {
        final ICPPASTFunctionDefinition containedFunction = getFunctionParent(testDouble);

        if (containedFunction != null && isValidTestFunction(containedFunction)) {
            return containedFunction;
        }

        return null;
    }

    private boolean isValidTestFunction(final ICPPASTFunctionDefinition function) {
        return FunctionsToAnalyze.fromProjectSettings(cProject.getProject()).shouldConsider(function);
    }

    private static ICPPASTFunctionDefinition getFunctionParent(final IASTNode astNode) {
        return CPPVisitor.findAncestorWithType(astNode, ICPPASTFunctionDefinition.class).orElse(null);
    }
}
