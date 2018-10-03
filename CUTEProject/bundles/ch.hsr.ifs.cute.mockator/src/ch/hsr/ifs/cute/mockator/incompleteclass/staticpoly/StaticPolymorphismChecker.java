package ch.hsr.ifs.cute.mockator.incompleteclass.staticpoly;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;

import ch.hsr.ifs.iltis.cpp.core.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.core.ast.checker.VisitorReport;
import ch.hsr.ifs.iltis.cpp.core.ast.visitor.SimpleVisitor;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.cute.mockator.ids.IdHelper.ProblemId;
import ch.hsr.ifs.cute.mockator.incompleteclass.checker.AbstractMissingMemFunChecker;


public class StaticPolymorphismChecker extends AbstractMissingMemFunChecker {

    @Override
    protected OnEachClass createVisitor() {
        return new OnEachClass();
    }

    private class OnEachClass extends SimpleVisitor<ProblemId, Void> {

        public OnEachClass() {
            super(StaticPolymorphismChecker.this);
        }

        {
            shouldVisitDeclSpecifiers = true;
        }

        @Override
        public int visit(final IASTDeclSpecifier specifier) {
            if (!ASTUtil.isClass(specifier)) {
                return PROCESS_CONTINUE;
            }

            final ICPPASTCompositeTypeSpecifier clazz = (ICPPASTCompositeTypeSpecifier) specifier;

            if (isNonTemplateClass(clazz)) {
                markIfHasMissingMemFuns(new VisitorReport<>(getProblemId(), clazz));
            }

            return PROCESS_CONTINUE;
        }

        @Override
        public Set<ProblemId> getProblemIds() {
            return EnumSet.of(ProblemId.STATIC_POLY_MISSING_MEMFUNS_IMPL);
        }

    }

    private static boolean isNonTemplateClass(final ICPPASTCompositeTypeSpecifier clazz) {
        return CPPVisitor.findAncestorWithType(clazz, ICPPASTTemplateDeclaration.class).orElse(null) == null;
    }

    @Override
    protected StaticPolyMissingMemFunFinder getMissingMemFunsFinder() {
        return new StaticPolyMissingMemFunFinder(getCProject(), getIndex());
    }

    @Override
    protected ProblemId getProblemId() {
        return ProblemId.STATIC_POLY_MISSING_MEMFUNS_IMPL;
    }

    @Override
    protected Optional<IASTName> getNameToMark(final ICPPASTCompositeTypeSpecifier clazz) {
        return Optional.of(clazz.getName());
    }
}
