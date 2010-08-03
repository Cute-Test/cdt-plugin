package ch.hsr.ifs.cute.ui;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IOption;


/**
 * @since 4.0
 */
public class IncludePathStrategy implements GetOptionsStrategy{

	public String[] getValues(IOption option) throws BuildException {
		return option.getIncludePaths();
	}
	
}