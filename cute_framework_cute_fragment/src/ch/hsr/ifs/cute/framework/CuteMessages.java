package ch.hsr.ifs.cute.framework;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import ch.hsr.ifs.test.framework.Messages;

/**
 * @since 3.1
 */
public class CuteMessages implements Messages {
	private static final String BUNDLE_NAME = "ch.hsr.ifs.cute.framework.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	public CuteMessages() {
	}

	/* (non-Javadoc)
	 * @see ch.hsr.ifs.cute.framework.Messages#getString(java.lang.String)
	 */
	public String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
