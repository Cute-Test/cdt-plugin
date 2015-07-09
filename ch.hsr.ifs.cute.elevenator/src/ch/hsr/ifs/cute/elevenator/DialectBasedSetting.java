package ch.hsr.ifs.cute.elevenator;

import java.util.ArrayList;
import java.util.List;

import ch.hsr.ifs.cute.elevenator.definition.IVersionModificationOperation;
import ch.hsr.ifs.cute.elevenator.operation.DoNothingOperation;

public final class DialectBasedSetting {
	private String name;
	private DialectBasedSetting parent = null;
	private List<DialectBasedSetting> subsettings = new ArrayList<DialectBasedSetting>();
	private IVersionModificationOperation operation;
	private boolean checked = false;

	public DialectBasedSetting(String name) {
		this(name, new DoNothingOperation());
	}

	public DialectBasedSetting(String name, IVersionModificationOperation operation) {
		this.name = name;
		this.operation = operation;

		// TODO: besseri Lösig finde!
		if (operation == null) {
			this.operation = new DoNothingOperation();
		}
	}

	private void setParent(DialectBasedSetting parent) {
		this.parent = parent;
	}

	public DialectBasedSetting getParent() {
		return parent;
	}

	public void addSubsetting(DialectBasedSetting subsetting) {
		subsetting.setParent(this);
		subsettings.add(subsetting);
	}

	public String getName() {
		return name;
	}

	public IVersionModificationOperation getOperation() {
		return operation;
	}

	public boolean hasSubsettings() {
		return !subsettings.isEmpty();
	}

	public List<DialectBasedSetting> getSubsettings() {
		return subsettings;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	@Override
	public String toString() {
		return name;
	}
}