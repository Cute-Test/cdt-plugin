package ch.hsr.ifs.cutelauncher;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
public class EclipseConsole {
	
	static MessageConsoleStream stream;
	static private MessageConsole console;
	
	public static MessageConsoleStream getConsole(){
		if(null == stream) {
			console = new MessageConsole("Cute Plugin Console",null);
			//,CuteLauncherPlugin.getImageDescriptor("obj16/cute_app.gif")); //FIXME
			console.activate();
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{ console });
			MessageConsoleStream s = console.newMessageStream();
			stream=s;
			
		}
		return stream;
	}
	
	public static void showConsole(){
		console.activate();
	}
	//stream.println("Hello, world!");
}
