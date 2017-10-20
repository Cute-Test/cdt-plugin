package ch.hsr.ifs.cute.headers;

import org.osgi.framework.Bundle;

import ch.hsr.ifs.cute.core.headers.CuteHeaders;

public abstract class BundledHeaders extends CuteHeaders {

	@Override
	public Bundle getBundle() {
		return CuteHeadersPlugin.getDefault().getBundle();
	}
}
