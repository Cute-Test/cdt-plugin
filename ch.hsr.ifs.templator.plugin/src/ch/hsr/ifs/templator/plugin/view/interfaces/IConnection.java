package ch.hsr.ifs.templator.plugin.view.interfaces;

import org.eclipse.swt.widgets.Composite;

public interface IConnection {

	Composite getConnectionStart();

	Composite getConnectionEnd();

	int getNameIndex();

	int getConnectionStartRectOffset();

	int getConnectionStartRectHeight();
}
