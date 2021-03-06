package ch.hsr.ifs.cute.mockator.project.cdt.options;

import static ch.hsr.ifs.iltis.core.collections.CollectionUtil.orderPreservingSet;

import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.core.resources.IProject;


public class IncludeFileHandler extends IncludeHandler {

    public IncludeFileHandler(final IProject project) {
        super(project);
    }

    @Override
    protected int getOptionType() {
        return IOption.INCLUDE_FILES;
    }

    @Override
    protected Set<String> getOptionValues(final IOption option) throws BuildException {
        final String[] includes = option.getBasicStringListValue();
        return orderPreservingSet(includes);
    }
}
