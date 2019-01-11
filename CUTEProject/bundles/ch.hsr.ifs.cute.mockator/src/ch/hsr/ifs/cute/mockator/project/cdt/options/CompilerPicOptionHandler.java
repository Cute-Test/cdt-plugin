package ch.hsr.ifs.cute.mockator.project.cdt.options;

import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.resources.IProject;


public class CompilerPicOptionHandler extends AbstractOptionsHandler {

    public CompilerPicOptionHandler(final IProject project) {
        super(project);
    }

    public void setPositionIndependentCode() {
        withEveryTool((tool, config) -> {
            for (final IOption option : tool.getOptions()) {
                if (isPicOption(option)) {
                    setAndSaveOption(config, tool, option, true);
                }
            }
            return null;
        });
    }

    private boolean isPicOption(final IOption option) {
        return option.getId().equals(projectVariables.getCompilerPicId());
    }

    @Override
    protected boolean isRequestedTool(final ITool tool) {
        return isCppCompiler(tool);
    }
}
