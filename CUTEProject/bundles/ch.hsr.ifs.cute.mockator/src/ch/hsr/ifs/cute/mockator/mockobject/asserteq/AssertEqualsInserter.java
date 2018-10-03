package ch.hsr.ifs.cute.mockator.mockobject.asserteq;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.cute.mockator.MockatorConstants;
import ch.hsr.ifs.cute.mockator.mockobject.support.context.MockSupportContext;
import ch.hsr.ifs.cute.mockator.project.nature.NatureHandler;


public class AssertEqualsInserter {

    private final MockSupportContext context;

    public AssertEqualsInserter(final MockSupportContext context) {
        this.context = context;
    }

    public void insertAssertEqual() {
        for (final ICPPASTFunctionDefinition testFunction : context.getReferencingFunctions()) {
            getAssertEqualsStrategy(testFunction).insertAssertEqual(context.getRewriter());
        }
    }

    private AbstractAssertEqualsInserter getAssertEqualsStrategy(final ICPPASTFunctionDefinition testFunction) {
        if (isCuteProject())
            return new CuteAssertEqualsInserter(testFunction, context);
        else return new CAssertEqualsInserter(testFunction, context);
    }

    private boolean isCuteProject() {
        return new NatureHandler(context.getProject().getProject()).hasNature(MockatorConstants.CUTE_NATURE);
    }
}
