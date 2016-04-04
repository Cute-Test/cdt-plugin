package ch.hsr.ifs.cute.templator.plugin.view.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import ch.hsr.ifs.cute.templator.plugin.view.interfaces.IConnection;
import ch.hsr.ifs.cute.templator.plugin.view.interfaces.IConnectionCollection;

public class TreeEntryCollection implements IConnectionCollection {

	private List<TreeSet<TreeEntry>> entries = new ArrayList<>();
	private Map<TreeEntry, TreeEntryCollectionNode> entryNodes = new HashMap<>();

	public void addRoot(TreeEntry rootEntry) {
		TreeEntryCollectionNode rootNode = new TreeEntryCollectionNode(rootEntry, new int[0], null);
		entryNodes.put(rootEntry, rootNode);
		addColumn(0);
		entries.get(0).add(rootEntry);
	}

	public void add(TreeEntry newEntry, TreeEntry parent, int nameIndex) {
		TreeEntryCollectionNode parentNode = entryNodes.get(parent);
		int[] newWeight = calculateWeight(parentNode.getWeight(), nameIndex);

		// Embed new node in tree
		TreeEntryCollectionNode newNode = new TreeEntryCollectionNode(newEntry, newWeight, parentNode);
		parentNode.addChild(newNode);
		entryNodes.put(newEntry, newNode);

		// Add new entry to rendering foundRegionList
		addColumn(newNode.getColumIdx());
		TreeSet<TreeEntry> column = entries.get(newNode.getColumIdx());
		column.add(newEntry);
	}

	private void removeChildren(TreeEntryCollectionNode node) {
		List<TreeEntryCollectionNode> childrenCopy = new ArrayList<>(node.getChildren());
		for (TreeEntryCollectionNode childNode : childrenCopy) {
			remove(childNode);
		}
	}

	private void removeFromParent(TreeEntryCollectionNode node) {
		if (node.getParent() != null) {
			node.getParent().getChildren().remove(node);
		}
	}

	private void remove(TreeEntryCollectionNode node) {
		removeChildren(node);
		removeFromParent(node);
		TreeSet<TreeEntry> column = entries.get(node.getColumIdx());
		column.remove(node.getEntry());
		if (column.size() == 0) {
			entries.remove(column);
		}
		entryNodes.remove(node.getEntry());
		node.getEntry().dispose();
	}

	public boolean remove(TreeEntry entry) {
		TreeEntryCollectionNode node = entryNodes.get(entry);
		if (node != null) {
			remove(node);
			return true;
		}
		return false;
	}

	public boolean remove(TreeEntry parent, int nameIndex) {
		TreeEntryCollectionNode parentNode = entryNodes.get(parent);
		for (TreeEntryCollectionNode childNode : parentNode.getChildren()) {
			if (childNode.getNameIndex() == nameIndex) {
				remove(childNode);
				return true;
			}
		}
		return false;
	}

	public void clear() {
		if (entries.size() > 0 && entries.get(0).size() > 0) {
			remove(entries.get(0).first());
		}
	}

	private void addColumn(int columnIdx) {
		while (entries.size() <= columnIdx) {
			entries.add(new TreeSet<TreeEntry>(new TreeEntryComp()));
		}
	}

	private int[] calculateWeight(int[] parentWeight, int nameIndex) {
		int[] newWeight = new int[parentWeight.length + 1];
		System.arraycopy(parentWeight, 0, newWeight, 0, parentWeight.length);
		newWeight[parentWeight.length] = nameIndex;
		return newWeight;
	}

	public List<TreeSet<TreeEntry>> getEntries() {
		return entries;
	}

	public List<TreeEntry> getAllSubEntries(TreeEntry treeEntry) {
		TreeEntryCollectionNode node = entryNodes.get(treeEntry);
		List<TreeEntry> subEntries = new ArrayList<>();
		for (TreeEntryCollectionNode childNode : node.getChildren()) {
			subEntries.add(childNode.getEntry());
		}
		return subEntries;
	}

	private class TreeEntryComp implements Comparator<TreeEntry> {

		@Override
		public int compare(TreeEntry e1, TreeEntry e2) {

			int[] weight1 = entryNodes.get(e1).getWeight();
			int[] weight2 = entryNodes.get(e2).getWeight();

			for (int i = 0; i < weight1.length; i++) {
				if (weight1[i] != weight2[i]) {
					return weight1[i] - weight2[i];
				}
			}
			return 0;
		}
	}

	@Override
	public Collection<IConnection> getConnections() {
		return new ArrayList<IConnection>(entryNodes.values());
	}
}
