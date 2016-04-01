package ch.hsr.ifs.templator.plugin.view.interfaces;

import ch.hsr.ifs.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.templator.plugin.util.ILoadingProgress;

public interface IAsyncLoadCallback {
	void loadOperation(ILoadingProgress loadingProgress) throws TemplatorException;

	void loadComplete();

	void loadException(Throwable throwable);
}
