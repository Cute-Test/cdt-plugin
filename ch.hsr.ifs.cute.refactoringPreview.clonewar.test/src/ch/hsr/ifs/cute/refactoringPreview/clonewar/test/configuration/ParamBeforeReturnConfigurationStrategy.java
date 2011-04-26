package ch.hsr.ifs.cute.refactoringPreview.clonewar.test.configuration;

import java.util.List;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;

import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.util.TypeInformation;

/**
 * Change the configuration to have the parameter generic type before the
 * return generic type in the template declaration.
 * 
 * @author ythrier(at)hsr.ch
 */
@SuppressWarnings("restriction")
public class ParamBeforeReturnConfigurationStrategy extends AbstractConfigurationStrategy {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void changeConfiguration(CRefactoring refactoring) {
		List<TypeInformation> types = getConfig(refactoring).getAllTypesOrdered();
		int i=0;
		for(TypeInformation type : types) {
			if(getConfig(refactoring).hasReturnTypeAction(type)) {
				type.setOrderId(types.size()-1);
			} else {
				type.setOrderId(i++);
			}
		}
	}
}
