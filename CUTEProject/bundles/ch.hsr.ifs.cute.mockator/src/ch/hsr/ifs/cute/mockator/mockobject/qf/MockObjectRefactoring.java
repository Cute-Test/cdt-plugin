package ch.hsr.ifs.cute.mockator.mockobject.qf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.iltis.cpp.core.wrappers.ModificationCollector;

import ch.hsr.ifs.cute.mockator.base.i18n.I18N;
import ch.hsr.ifs.cute.mockator.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.cute.mockator.mockobject.MockObject;
import ch.hsr.ifs.cute.mockator.mockobject.support.MockSupportAdder;
import ch.hsr.ifs.cute.mockator.mockobject.support.context.MockSupportContext;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.cute.mockator.refsupport.utils.ClassPublicVisibilityInserter;
import ch.hsr.ifs.cute.mockator.testdouble.entities.TestDouble;
import ch.hsr.ifs.cute.mockator.testdouble.qf.AbstractTestDoubleRefactoring;


public class MockObjectRefactoring extends AbstractTestDoubleRefactoring {

    private final Collection<MissingMemberFunction> missingMemFuns;
    private final LinkedEditModeStrategy            linkedEdit;

    public MockObjectRefactoring(final CppStandard cppStd, final ICElement cElement, final Optional<ITextSelection> selection,
                                 final ICProject cProject, final LinkedEditModeStrategy linkedEdit) {
        super(cppStd, cElement, selection, cProject);
        this.linkedEdit = linkedEdit;
        missingMemFuns = new ArrayList<>();
    }

    @Override
    protected void collectModifications(final IProgressMonitor pm, final ModificationCollector collector) throws CoreException,
            OperationCanceledException {
        final IASTTranslationUnit ast = getAST(tu, pm);
        final ASTRewrite rewriter = collector.rewriterForTranslationUnit(ast);
        missingMemFuns.addAll(collectMissingMemFuns(pm));
        final ClassPublicVisibilityInserter inserter = getPublicVisibilityInserter(rewriter);
        addMockSupport(ast, rewriter, inserter, pm);
        testDouble.addMissingMemFuns(missingMemFuns, inserter, cppStd);
    }

    private boolean hasOnlyStaticMemFuns() {
        return testDouble.hasOnlyStaticFunctions(missingMemFuns);
    }

    private void addMockSupport(final IASTTranslationUnit ast, final ASTRewrite r, final ClassPublicVisibilityInserter ci,
            final IProgressMonitor pm) {
        final MockSupportAdder adder = new MockSupportAdder(buildContext(r, ast, ci, pm));
        adder.addMockSupport();
    }

    private MockSupportContext buildContext(final ASTRewrite rewriter, final IASTTranslationUnit ast, final ClassPublicVisibilityInserter inserter,
            final IProgressMonitor pm) {
        return new MockSupportContext.ContextBuilder(getProject(), refactoringContext, (MockObject) testDouble, rewriter, ast, cppStd, inserter,
                hasOnlyStaticMemFuns(), pm).withLinkedEditStrategy(linkedEdit).withNewExpectations(missingMemFuns).build();
    }

    Collection<MissingMemberFunction> getMemberFunctionsForLinkedEdit() {
        return missingMemFuns;
    }

    @Override
    public String getDescription() {
        return I18N.MockObjectRefactoringDesc;
    }

    @Override
    protected TestDouble createTestDouble(final ICPPASTCompositeTypeSpecifier selectedClass) {
        return new MockObject(selectedClass);
    }
}
