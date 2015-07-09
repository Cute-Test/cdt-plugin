package ch.hsr.ifs.cute.elevenator;

import java.util.ArrayList;
import java.util.List;

import ch.hsr.ifs.cute.elevenator.definition.IVersionModificationOperation;

public final class DialectBasedSetting {
	private String name;
	private DialectBasedSetting parent = null;
	private List<DialectBasedSetting> subsettings = new ArrayList<DialectBasedSetting>();
	private IVersionModificationOperation operation;
	private boolean checked = false;

	public DialectBasedSetting(String name) {
		this(name, null);
	}

	public DialectBasedSetting(String name, IVersionModificationOperation operation) {
		this.name = name;
		this.operation = operation;
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

		checkedChangedDowntree(this);

		if (getParent() != null) {
			getParent().checkedChangedUptree();
		}
	}

	private void checkedChangedDowntree(DialectBasedSetting setting) {
		for (DialectBasedSetting subsetting : setting.getSubsettings()) {
			subsetting.checked = setting.checked;
			subsetting.checkedChangedDowntree(subsetting);
		}
	}

	private void checkedChangedUptree() {
		if (getCheckedChildCount() == getSubsettings().size()) {
			this.checked = true;
		} else {
			this.checked = false;
		}
		if (getParent() != null) {
			getParent().checkedChangedUptree();
		}
	}

	public int getCheckedChildCount() {
		int count = 0;
		for (DialectBasedSetting subsetting : getSubsettings()) {
			if (subsetting.isChecked()) {
				count++;
			}
		}
		return count;
	}

	@Override
	public String toString() {
		return name;
	}
}