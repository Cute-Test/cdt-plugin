/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.core.launch;

import ch.hsr.ifs.cute.core.CuteCorePlugin;
import ch.hsr.ifs.cute.core.event.CuteConsoleEventParser;
import ch.hsr.ifs.testframework.event.ConsoleEventParser;
import ch.hsr.ifs.testframework.launch.TestLauncherDelegate;


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
