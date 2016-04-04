package ch.hsr.ifs.cute.templator.plugin.asttools;

import org.eclipse.cdt.core.index.IIndex;

public interface IIndexAction {
	<T extends Object> T doAction(IIndex index) throws Exception;
}
