/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.model;

/**
 * @author Emanuel Graf
 * 
 */
public class NotifyEvent {

	public enum EventType {
		testFinished, suiteFinished
	}

	private final EventType type;

	private final TestElement element;

	public NotifyEvent(EventType type, TestElement element) {
		super();
		this.type = type;
		this.element = element;
	}

	public EventType getType() {
		return type;
	}

	public TestElement getElement() {
		return element;
	}

}
