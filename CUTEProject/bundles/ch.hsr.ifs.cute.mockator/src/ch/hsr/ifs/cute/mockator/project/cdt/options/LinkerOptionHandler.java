package ch.hsr.ifs.cute.mockator.project.cdt.options;

import static ch.hsr.ifs.iltis.core.collections.CollectionUtil.orderPreservingSet;

import java.util.Collection;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.iltis.core.functional.functions.Function2;


public class LinkerOptionHandler extends AbstractOptionsHandler {

    public LinkerOptionHandler(final IProject project) {
        super(project);
    }

    public void removeLinkerFlag(final String flagName) {
        toggleLinkerFlag(new LinkerOtherFlagRemover(), flagName);
    }

    public void addLinkerFlag(final String flagName) {
        toggleLinkerFlag(new LinkerOtherFlagAdder(), flagName);
    }

    public boolean hasLinkerFlag(final String flagName) {
        return getToolToAnanalyze().map(tool -> {
            final IOption flagsOption = tool.getOptionBySuperClassId(projectVariables.getLinkerOtherFlags());

            if (flagsOption == null) {
                return false;
            }

            final Collection<String> currentFlags = getListValues(flagsOption);

            return currentFlags.contains(flagName);
        }).orElse(false);
    }

    private void toggleLinkerFlag(final Function2<String, Collection<String>, Void> linkerFlagOp, final String flagName) {
        withEveryTool((tool, config) -> {
            final IOption flagsOption = tool.getOptionBySuperClassId(projectVariables.getLinkerOtherFlags());
            final Collection<String> linkerOptions = getListValues(flagsOption);
            linkerFlagOp.apply(flagName, linkerOptions);
            setAndSaveOption(config, tool, flagsOption, linkerOptions);
            return null;
        });
    }

    private static Collection<String> getListValues(final IOption option) {
        try {
            return orderPreservingSet(option.getStringListValue());
        } catch (final BuildException e) {
            throw new ILTISException(e).rethrowUnchecked();
        }
    }

    @Override
    protected boolean isRequestedTool(final ITool tool) {
        return isLinker(tool);
    }

    private static class LinkerOtherFlagAdder implements Function2<String, Collection<String>, Void> {

        @Override
        public Void apply(final String wrapFunName, final Collection<String> flags) {
            flags.add(wrapFunName);
            return null;
        }
    }

    private static class LinkerOtherFlagRemover implements Function2<String, Collection<String>, Void> {

        @Override
        public Void apply(final String wrapFunName, final Collection<String> flags) {
            flags.remove(wrapFunName);
            return null;
        }
    }
}
