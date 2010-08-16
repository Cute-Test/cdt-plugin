/*******************************************************************************
 * Copyright (c) 2007, 2010 Institute for Software, HSR Hochschule für Technik  
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Emanuel Graf - initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cute.core.launch;



import ch.hsr.ifs.cute.core.CuteCorePlugin;
import ch.hsr.ifs.cute.core.event.CuteConsoleEventParser;
import ch.hsr.ifs.test.framework.event.ConsoleEventParser;
import ch.hsr.ifs.test.framework.launch.TestLauncherDelegate;
/**
 * @author egraf
 *
 */
public class CuteLauncherDelegate extends TestLauncherDelegate {

	@Override
	protected String getPluginID() {
		return CuteCorePlugin.getUniqueIdentifier();
	}

	@Override
	protected ConsoleEventParser getConsoleEventParser() {
		return new CuteConsoleEventParser();
	}
	
}
