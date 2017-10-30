package ch.hsr.ifs.mockator.plugin.project.cdt.options;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;

import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.core.resources.IProject;


public class IncludeFileHandler extends IncludeHandler {

   public IncludeFileHandler(IProject project) {
      super(project);
   }

   @Override
   protected int getOptionType() {
      return IOption.INCLUDE_FILES;
   }

   @Override
   protected Set<String> getOptionValues(IOption option) throws BuildException {
      String[] includes = option.getBasicStringListValue();
      return orderPreservingSet(includes);
   }
}
