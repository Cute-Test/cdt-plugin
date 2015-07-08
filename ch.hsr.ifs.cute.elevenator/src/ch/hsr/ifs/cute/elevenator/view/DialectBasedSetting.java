package ch.hsr.ifs.cute.elevenator.view;

import java.util.ArrayList;
import java.util.List;

import ch.hsr.ifs.cute.elevenator.definition.IVersionModificationOperation;
import ch.hsr.ifs.cute.elevenator.operation.DoNothingOperation;

public final class DialectBasedSetting {
	private String name;
	private List<DialectBasedSetting> subsettings = new ArrayList<DialectBasedSetting>();
	private IVersionModificationOperation operation;

	public DialectBasedSetting(String name) {
		this(name, new DoNothingOperation());
	}

	public DialectBasedSetting(String name, IVersionModificationOperation operation) {
		this.name = name;
		this.operation = operation;
	}

	public void addSubsetting(DialectBasedSetting subsetting) {
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

	@Override
	public String toString() {
		return name;
	}
}