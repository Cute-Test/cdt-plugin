package ch.hsr.ifs.templator.plugin.util;

public interface ILoadingProgress {
	void setProgress(double d);

	void setStatus(String statusText);
}
