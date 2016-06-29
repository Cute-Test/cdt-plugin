package ch.hsr.ifs.cute.templator.plugin.view.interfaces;

public interface IRectangleContextMenuActionHandler {

	public enum RectangleContextAction {
		OPEN_CLOSE("Open/Close Link"),
		GO_TO_SOURCE("Go to Source");

		private String text;

		private RectangleContextAction(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}

	void contextActionPerformed(int rectangleIndex, RectangleContextAction action);
}
