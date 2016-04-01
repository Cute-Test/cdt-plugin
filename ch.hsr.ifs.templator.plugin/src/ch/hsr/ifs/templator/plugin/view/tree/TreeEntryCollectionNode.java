package ch.hsr.ifs.templator.plugin.view.tree;

import java.util.ArrayList;
import java.util.List;

import ch.hsr.ifs.templator.plugin.view.interfaces.IConnection;

public class TreeEntryCollectionNode implements IConnection {
	private TreeEntry entry;
	private TreeEntryCollectionNode parent;
	private List<TreeEntryCollectionNode> children = new ArrayList<>();

	/**
	 * This is used for calculating the Order of Entries in one column of the view. The integer array represents the
	 * originating sub entry index in the parent entry. In the last array position the sub entry index belonging to this
	 * entry can be found. The array size corresponds to the column index.
	 * 
	 * Detailed information in the documentation in chapter 'The evolution of the view' under chapter 'Sorting of
	 * columns'
	 */
	private int[] weight;

	public TreeEntryCollectionNode(TreeEntry entry, int[] weight, TreeEntryCollectionNode parent) {
		this.entry = entry;
		this.weight = weight;
		this.parent = parent;
	}

	public TreeEntry getEntry() {
		return entry;
	}

	public int[] getWeight() {
		return weight;
	}

	public TreeEntryCollectionNode getParent() {
		return parent;
	}

	public void addChild(TreeEntryCollectionNode child) {
		children.add(child);
	}

	public List<TreeEntryCollectionNode> getChildren() {
		return children;
	}

	public int getColumIdx() {
		return weight.length;
	}

	@Override
	public int getNameIndex() {
		return weight[weight.length - 1];
	}

	@Override
	public TreeEntry getConnectionStart() {
		return getParent() == null ? null : getParent().getEntry();
	}

	@Override
	public TreeEntry getConnectionEnd() {
		return getEntry();
	}

	@Override
	public int getConnectionStartRectOffset() {
		return getConnectionStart().getRectOffset(getNameIndex());
	}

	@Override
	public int getConnectionStartRectHeight() {
		return getConnectionStart().getRectHeight(getNameIndex());
	}
}