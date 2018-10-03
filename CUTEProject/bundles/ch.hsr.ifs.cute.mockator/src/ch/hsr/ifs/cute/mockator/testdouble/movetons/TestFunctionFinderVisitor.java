package ch.hsr.ifs.cute.mockator.testdouble.movetons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.cute.mockator.project.properties.FunctionsToAnalyze;


class TestFunctionFinderVisitor extends ASTVisitor {

    private final List<IASTFunctionDefinition> functions;
    private final FunctionsToAnalyze           strategy;

    {
        shouldVisitDeclarations = true;
    }

    public TestFunctionFinderVisitor(final FunctionsToAnalyze strategy) {
        this.strategy = strategy;
        functions = new ArrayList<>();
    }

    public Collection<IASTFunctionDefinition> getFunctions() {
        return functions;
    }

    @Override
    public int visit(final IASTDeclaration declaration) {
        if (!(declaration instanceof ICPPASTFunctionDefinition)) return PROCESS_CONTINUE;

        final ICPPASTFunctionDefinition function = (ICPPASTFunctionDefinition) declaration;

        if (!isFunctionOfLocalClass(function) && strategy.shouldConsider(function)) {
            functions.add((IASTFunctionDefinition) declaration);
        }

        return PROCESS_CONTINUE;
    }

    private static boolean isFunctionOfLocalClass(final ICPPASTFunctionDefinition function) {
        return CPPVisitor.findAncestorWithType(function.getParent(), ICPPASTFunctionDefinition.class).orElse(null) != null;
    }
}
