package ch.hsr.ifs.cute.core.launch;

import ch.hsr.ifs.cute.core.event.CuteConsoleEventParser;
import ch.hsr.ifs.testframework.event.ConsoleEventParser;
import ch.hsr.ifs.testframework.launch.TestLauncherDelegate;


/**
 * Launch delegate implementation that redirects its queries to the preferred launch delegate, correcting the arguments attribute (to take into
 * account auto generated test module parameters) and
 * setting up the custom process factory (to handle testing process IO streams).
 */
public class CuteDebugLauncherDelegate extends TestLauncherDelegate {

    public String getPreferredDelegateId() {
        return "org.eclipse.cdt.dsf.gdb.launch.localCLaunch";
    }
    
    protected ConsoleEventParser getConsoleEventParser() {
        return new CuteConsoleEventParser();
    }

}
