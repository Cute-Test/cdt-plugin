package ch.hsr.ifs.cute.elevenator.view;

import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

import ch.hsr.ifs.cute.elevenator.DialectBasedSetting;

public final class DialectBasedSettingsProvider implements ITreeContentProvider, ILabelProvider {
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof DialectBasedSetting) {
			return ((DialectBasedSetting) element).hasSubsettings();
		}
		return false;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof DialectBasedSetting) {
			return ((DialectBasedSetting) element).getParent();
		}
		return null;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof DialectBasedSetting) {
			List<DialectBasedSetting> subsettings = ((DialectBasedSetting) parentElement).getSubsettings();
			return subsettings.toArray(new Object[subsettings.size()]);
		}
		return null;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Image getImage(Object element) {
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof DialectBasedSetting) {
			return ((DialectBasedSetting) element).getName();
		}
		return "<unknown>";
	}
}