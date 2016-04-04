package ch.hsr.ifs.cute.templator.plugin.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public final class ColorPalette {

	private static Map<Integer, Color> colorTable = null;
	private static Map<Integer, Color> brightColorTable = null;

	private ColorPalette() {
	}

	public static Color getColor(int colorId) {
		if (colorTable == null) {
			initPalette();
		}
		int absoluteColorId = colorId % colorTable.size();
		return colorTable.get(absoluteColorId);
	}

	public static Color getBrightColor(int colorId) {
		if (brightColorTable == null) {
			initBrightPalette();
		}
		int absoluteColorId = colorId % brightColorTable.size();
		return brightColorTable.get(absoluteColorId);
	}

	private static void initPalette() {
		colorTable = new HashMap<Integer, Color>();
		colorTable.put(0, new Color(Display.getCurrent(), new RGB(250, 162, 27)));
		colorTable.put(1, new Color(Display.getCurrent(), new RGB(4, 98, 93)));
		colorTable.put(2, new Color(Display.getCurrent(), new RGB(162, 100, 65)));
		colorTable.put(3, new Color(Display.getCurrent(), new RGB(233, 221, 45)));
		colorTable.put(4, new Color(Display.getCurrent(), new RGB(0, 120, 184)));
		colorTable.put(5, new Color(Display.getCurrent(), new RGB(242, 101, 34)));
		colorTable.put(6, new Color(Display.getCurrent(), new RGB(229, 128, 174)));
	}

	private static void initBrightPalette() {

		brightColorTable = new HashMap<Integer, Color>();
		brightColorTable.put(0, new Color(Display.getCurrent(), new RGB(255, 200, 60)));
		brightColorTable.put(1, new Color(Display.getCurrent(), new RGB(20, 200, 180)));
		brightColorTable.put(2, new Color(Display.getCurrent(), new RGB(240, 150, 100)));
		brightColorTable.put(3, new Color(Display.getCurrent(), new RGB(255, 240, 80)));
		brightColorTable.put(4, new Color(Display.getCurrent(), new RGB(60, 190, 240)));
		brightColorTable.put(5, new Color(Display.getCurrent(), new RGB(255, 150, 60)));
		brightColorTable.put(6, new Color(Display.getCurrent(), new RGB(255, 180, 220)));
	}

	public static void disposePalette() {
		if (colorTable == null) {
			return;
		}
		for (Color color : colorTable.values()) {
			color.dispose();
		}
		colorTable = null;

		if (brightColorTable == null) {
			return;
		}
		for (Color color : brightColorTable.values()) {
			color.dispose();
		}
		brightColorTable = null;
	}
}
