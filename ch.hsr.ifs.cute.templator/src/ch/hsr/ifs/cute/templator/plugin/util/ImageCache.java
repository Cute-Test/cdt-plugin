package ch.hsr.ifs.cute.templator.plugin.util;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import ch.hsr.ifs.cute.templator.plugin.TemplatorPlugin;

public final class ImageCache {

	private ImageCache() {
	}

	public enum ImageID {
		HAND_CLOSED,
		HAND_OPEN,
		ICON_CON_MAX,
		ICON_CON_MIN,
		ICON_MENU,
		ICON_REMOVE,
		LOADING,
		PROGRESS_INDICATOR,
		MINIMIZE_ALL,
		MAXIMIZE_ALL,
		REFRESH,
		CLOSE_ALL,
		ARROW_UP,
		ARROW_DOWN
	}

	private static ImageRegistry imageRegistry = TemplatorPlugin.getDefault().getImageRegistry();

	static {
		addImage(ImageID.HAND_OPEN, "/icons/hand_open.png");
		addImage(ImageID.HAND_CLOSED, "/icons/hand_closed.png");
		addImage(ImageID.ICON_CON_MAX, "/icons/icon_con_max.png");
		addImage(ImageID.ICON_CON_MIN, "/icons/icon_con_min.png");
		addImage(ImageID.ICON_MENU, "/icons/icon_menu_white.png");
		addImage(ImageID.ICON_REMOVE, "/icons/icon_remove.png");
		addImage(ImageID.LOADING, "/icons/loading.png");
		addImage(ImageID.PROGRESS_INDICATOR, "/icons/progress_indicator.gif");
		addImage(ImageID.MINIMIZE_ALL, "/icons/minimize_all.png");
		addImage(ImageID.MAXIMIZE_ALL, "/icons/maximize_all.png");
		addImage(ImageID.REFRESH, "/icons/icon_refresh.png");
		addImage(ImageID.CLOSE_ALL, "/icons/close_all.png");
		addImage(ImageID.ARROW_UP, "/icons/arrow_up.png");
		addImage(ImageID.ARROW_DOWN, "/icons/arrow_down.png");
	}

	public static Image get(ImageID id) {
		return imageRegistry.get(id.toString());
	}

	private static void addImage(ImageID id, String filename) {
		Image image = new Image(Display.getCurrent(), ImageCache.class.getResourceAsStream(filename));
		imageRegistry.put(id.toString(), image);
	}

}
