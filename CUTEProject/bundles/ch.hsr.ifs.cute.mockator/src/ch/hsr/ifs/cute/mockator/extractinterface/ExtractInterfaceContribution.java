package ch.hsr.ifs.cute.mockator.extractinterface;

import java.util.Map;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import ch.hsr.ifs.iltis.core.core.functional.functions.Function;

import ch.hsr.ifs.iltis.cpp.core.resources.info.IInfo;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CRefactoringContribution;

import ch.hsr.ifs.cute.mockator.ids.IdHelper.RefactoringId;
import ch.hsr.ifs.cute.mockator.infos.ExtractInterfaceInfo;


public class ExtractInterfaceContribution extends CRefactoringContribution<RefactoringId> {

    @Override
    public RefactoringDescriptor createDescriptor(RefactoringId id, String project, String description, String comment, Map<String, String> arguments,
            int flags) throws IllegalArgumentException {
        return id == RefactoringId.EXTRACT_INTERFACE ? new ExtractInterfaceDescriptor(project, description, comment, IInfo.fromMap(
                ExtractInterfaceInfo::new, arguments)) : null;
    }

    @Override
    protected Function<String, RefactoringId> getFromStringMethod() {
        return RefactoringId::of;
    }

}
