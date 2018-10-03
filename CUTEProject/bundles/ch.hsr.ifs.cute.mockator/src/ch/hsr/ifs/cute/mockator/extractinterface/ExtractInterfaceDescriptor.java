package ch.hsr.ifs.cute.mockator.extractinterface;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CRefactoring;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CRefactoringDescriptor;

import ch.hsr.ifs.cute.mockator.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.cute.mockator.ids.IdHelper.RefactoringId;
import ch.hsr.ifs.cute.mockator.infos.ExtractInterfaceInfo;


public class ExtractInterfaceDescriptor extends CRefactoringDescriptor<RefactoringId, ExtractInterfaceInfo> {

    public static final String NEW_INTERFACE_NAME     = "name";
    public static final String REPLACE_ALL_OCCURENCES = "replace";

    protected ExtractInterfaceDescriptor(final String project, final String description, final String comment, ExtractInterfaceInfo info) {
        super(RefactoringId.EXTRACT_INTERFACE, project, description, comment, RefactoringDescriptor.MULTI_CHANGE, info);
    }

    @Override
    public CRefactoring createRefactoring(final RefactoringStatus status) throws CoreException {
        return new ExtractInterfaceRefactoring(createMockatorContext(status));
    }

    private ExtractInterfaceContext createMockatorContext(final RefactoringStatus status) throws CoreException {
        final boolean doReplace = info.replaceAllOccurences;
        final String interfaceName = info.interfaceName;
        return new ExtractInterfaceContext.ContextBuilder(getTranslationUnit(), info.getSelection(), getCProject()).withRefactoringStatus(status)
                .replaceAllOccurences(doReplace).withNewInterfaceName(interfaceName).build();
    }
}
