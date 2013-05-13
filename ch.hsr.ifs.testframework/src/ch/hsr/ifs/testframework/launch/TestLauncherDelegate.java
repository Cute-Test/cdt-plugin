/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.launch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.progress.UIJob;

import ch.hsr.ifs.testframework.TestFrameworkPlugin;
import ch.hsr.ifs.testframework.event.ConsoleEventParser;
import ch.hsr.ifs.testframework.event.TestEventHandler;
import ch.hsr.ifs.testframework.model.ModellBuilder;
import ch.hsr.ifs.testframework.ui.ConsoleLinkHandler;
import ch.hsr.ifs.testframework.ui.ShowResultView;

/**
 * @since 3.0
 */
@SuppressWarnings("restriction")
public abstract class TestLauncherDelegate extends AbstractCLaunchDelegate {

	public TestLauncherDelegate() {
		super();
	}

	protected abstract ConsoleEventParser getConsoleEventParser();

	@Override
	protected abstract String getPluginID();

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if(monitor == null) {
			monitor = new NullProgressMonitor();
		}
		runLocalApplication(configuration, launch, monitor);
	}

	private void runLocalApplication(ILaunchConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		
		monitor.beginTask( LaunchMessages.LocalCDILaunchDelegate_0, 10 );
		if ( monitor.isCanceled() ) {
			return;
		}
		monitor.worked( 1 );
		try {
			IPath exePath=CDebugUtils.verifyProgramPath( config );
			ICProject cProject = CDebugUtils.verifyCProject(config);
			IProject project = cProject.getProject();
			notifyBeforeLaunch(project);
			File wd = getWorkingDirectory( config );
			if ( wd == null ) {
				wd = new File( System.getProperty( "user.home", "." ) ); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			String arguments[] = getProgramArgumentsArray( config );
			ArrayList<String> command = new ArrayList<String>( 1 + arguments.length );
			command.add( exePath.toOSString() );
			command.addAll( Arrays.asList( arguments ) );
			String[] commandArray = command.toArray( new String[command.size()] );
			boolean usePty = config.getAttribute("ch.hsr.ifs.testframework.launcher.useTerminal", true); //$NON-NLS-1$
			monitor.worked( 2 );
			Process process = exec( commandArray, this.getEnvironment( config ), wd, usePty );
			monitor.worked( 6 );
			DebugPlugin.newProcess( launch, process, renderProcessLabel( commandArray[0] ) );
			IProcess proc = launch.getProcesses()[0];
			IConsole console = DebugUITools.getConsole(proc);
			if (console instanceof TextConsole) {
				UIJob job = new ShowResultView();
				job.schedule();
				try {
					job.join();
				} catch (InterruptedException e) {
				}
				TextConsole textCons = (TextConsole) console;
	
				exePath=sourcelookupPath(config,exePath);
				
				registerPatternMatchListener(launch, exePath, textCons);
			}
	
			notifyAfterLaunch(project);
		}
		finally {
			monitor.done();
		}		
	}

	/**
	 * @since 3.0
	 */
	protected void registerPatternMatchListener(ILaunch launch, IPath exePath, TextConsole textCons) {
		TestEventHandler handler = new ConsoleLinkHandler(exePath, textCons);
		ModellBuilder modelHandler = new ModellBuilder(exePath, launch);
		ConsolePatternListener listener = new ConsolePatternListener(getConsoleEventParser());
		listener.addHandler(handler);
		listener.addHandler(modelHandler);
		textCons.addPatternMatchListener(listener);
	}

	/**
	 * @since 3.0
	 */
	protected void notifyAfterLaunch(IProject project) throws CoreException {
		for (ILaunchObserver observer : getObservers()) {
			observer.notifyAfterLaunch(project);
		}
	}

	/**
	 * @since 3.0
	 */
	protected void notifyBeforeLaunch(IProject project) throws CoreException {
		for (ILaunchObserver observer : getObservers()) {
			observer.notifyBeforeLaunch(project);
		}
	}

	private List<ILaunchObserver> getObservers() {
		List<ILaunchObserver> additions = new ArrayList<ILaunchObserver>();
		try{
			IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(TestFrameworkPlugin.PLUGIN_ID, "launchObserver"); //$NON-NLS-1$
			if (extension != null) {
				IExtension[] extensions = extension.getExtensions();
				for (IExtension extension2 : extensions) {
					IConfigurationElement[] configElements = extension2.getConfigurationElements();
					String className =configElements[0].getAttribute("class"); //$NON-NLS-1$
					Class<?> obj = Platform.getBundle(extension2.getContributor().getName()).loadClass(className);
					additions.add((ILaunchObserver) obj.newInstance());
				}
			}
		} catch (ClassNotFoundException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		}
		return additions;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected String[] getEnvironment(ILaunchConfiguration config) throws CoreException {
		Map map = config.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map)null);
		if(map==null)return super.getEnvironment( config );
		
		String[] array = DebugPlugin.getDefault().getLaunchManager().getEnvironment(config);
		if (array == null) {
			return new String[0];
		}
		return array;
		
	}

	public IPath sourcelookupPath(ILaunchConfiguration config, IPath exePath) {
		try{
		if(config!=null && config.getAttribute(CustomisedLaunchConfigTab.USE_CUSTOM_SRC_PATH, false)){
			String rootpath=org.eclipse.core.runtime.Platform.getLocation().toOSString();
			String customSrcPath=config.getAttribute(CustomisedLaunchConfigTab.CUSTOM_SRC_PATH,""); //$NON-NLS-1$
			String fileSeparator=System.getProperty("file.separator"); //$NON-NLS-1$
			return new org.eclipse.core.runtime.Path(rootpath+customSrcPath+fileSeparator);
		}else{
			return exePath.removeLastSegments(1);
		}}catch(CoreException ce){TestFrameworkPlugin.getDefault().getLog().log(ce.getStatus());}
		return exePath;//on error, log and make no changes
	}

	protected Process exec(String[] cmdLine, String[] environ, File workingDirectory, boolean usePty) throws CoreException {
		Process p = null;
		try {
			if ( workingDirectory == null ) {
				p = ProcessFactory.getFactory().exec( cmdLine, environ );
			}
			else {
				if ( usePty && PTY.isSupported() ) {
					p = ProcessFactory.getFactory().exec( cmdLine, environ, workingDirectory, new PTY() );
				}
				else {
					p = ProcessFactory.getFactory().exec( cmdLine, environ, workingDirectory );
				}
			}
		}
		catch( IOException e ) {
			abort("IOException in exec()", e, 99); //$NON-NLS-1$
		}
		catch( NoSuchMethodError e ) {
			// attempting launches on 1.2.* - no ability to set working
			// directory
			IStatus status = new Status( IStatus.ERROR, LaunchUIPlugin.getUniqueIdentifier(), 98, "Eclipse runtime does not support working directory.", e ); //$NON-NLS-1$
			IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler( status );
			if ( handler != null ) {
				Object result = handler.handleStatus( status, this );
				if ( result instanceof Boolean && ((Boolean)result).booleanValue() ) {
					p = exec( cmdLine, environ, null, usePty );
				}
			}
		}
		return p;
	}

}