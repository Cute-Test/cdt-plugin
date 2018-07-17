package ch.hsr.ifs.cute.mockator.project.cdt.options;

import static ch.hsr.ifs.iltis.core.core.collections.CollectionUtil.orderPreservingSet;

import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.core.resources.IProject;


public class IncludePathHandler extends IncludeHandler {

   public IncludePathHandler(final IProject project) {
      super(project);
   }

   @Override
   protected int getOptionType() {
      return IOption.INCLUDE_PATH;
   }

   @Override
   protected Set<String> getOptionValues(final IOption option) throws BuildException {
      return orderPreservingSet(option.getIncludePaths());
   }
}
