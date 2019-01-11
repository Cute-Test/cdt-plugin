package ch.hsr.ifs.cute.mockator.incompleteclass.staticpoly.referenced;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;

import ch.hsr.ifs.iltis.cpp.core.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.cute.mockator.incompleteclass.StaticPolyMissingMemFun;
import ch.hsr.ifs.cute.mockator.refsupport.functions.FunctionEquivalenceVerifier;


public class NotReferencedFunctionFilter implements Predicate<StaticPolyMissingMemFun> {

    private final Map<ICPPASTFunctionDefinition, Boolean> cache;
    private final FunctionCalleeReferenceResolver         calleeReferenceResolver;
    private final ICPPASTFunctionDefinition               testFunction;

    public NotReferencedFunctionFilter(final IIndex index, final ICProject cProject, final ICPPASTFunctionDefinition testFunction) {
        this.testFunction = testFunction;
        calleeReferenceResolver = new FunctionCalleeReferenceResolver(index, cProject);
        cache = new HashMap<>();
    }

    @Override
    public boolean test(final StaticPolyMissingMemFun memFunCall) {
        final ICPPASTFunctionDefinition sutFunction = memFunCall.getContainingFunction();

        if (!shouldConsider(sutFunction)) {
            return true;
        }

        Boolean called = cache.get(sutFunction);

        if (called == null) {
            called = isCalled(sutFunction);
            cache.put(sutFunction, called);
        }

        return called;
    }

    private static boolean shouldConsider(final ICPPASTFunctionDefinition sutFunction) {
        return sutFunction != null && !ASTUtil.isConstructor(sutFunction);
    }

    private boolean isCalled(final ICPPASTFunctionDefinition sutFunction) {
        final IBinding sutBinding = sutFunction.getDeclarator().getName().resolveBinding();

        for (final IASTName caller : calleeReferenceResolver.findCallers(sutBinding, sutFunction)) {
            if (matches(testFunction, getFunctionDefinition(caller))) {
                return true;
            }
        }

        return false;
    }

    private ICPPASTFunctionDefinition getFunctionDefinition(final IASTName caller) {
        return CPPVisitor.findAncestorWithType(caller, ICPPASTFunctionDefinition.class).orElse(null);
    }

    private static boolean matches(final ICPPASTFunctionDefinition functionInUse, final ICPPASTFunctionDefinition missingMemFun) {
        if (functionInUse == null || missingMemFun == null) {
            return false;
        }

        final FunctionEquivalenceVerifier checker = new FunctionEquivalenceVerifier((ICPPASTFunctionDeclarator) functionInUse.getDeclarator());
        return checker.isEquivalent((ICPPASTFunctionDeclarator) missingMemFun.getDeclarator());
    }
}
