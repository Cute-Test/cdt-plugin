/*******************************************************************************
 * Copyright (c) 2008, Industrial Logic, Inc. All Rights Reserved.
 * Copyright (c) 2018, IFS Institute for Software
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Industrial Logic, Inc.: Mike Bria & John Tangney - initial implementation (based on ideas originating from the work of Emanuel Graf)
 * IFS Institute for Software: Felix Morgner <fmorgner@hsr.ch> - Kotlin port 
 ******************************************************************************/
package ch.hsr.ifs.testframework.event

class NonEvent : TestEvent

class SessionEndEvent : TestEvent

class SessionStartEvent : TestEvent
