/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule für Technik  
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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

import ch.hsr.ifs.cute.core.CuteCorePlugin;
import ch.hsr.ifs.cute.core.event.CuteConsoleEventParser;
import ch.hsr.ifs.cute.gcov.GcovPlugin;
import ch.hsr.ifs.cute.gcov.parser.LineCoverageParser;
import ch.hsr.ifs.cute.gcov.parser.ModelBuilderLineParser;
import ch.hsr.ifs.test.framework.ConsolePatternListener;
import ch.hsr.ifs.test.framework.event.ConsoleEventParser;
import ch.hsr.ifs.test.framework.model.ModellBuilder;
import ch.hsr.ifs.test.framework.ui.ConsoleLinkHandler;
import ch.hsr.ifs.test.framework.ui.ShowResultView;
/**
 * @author egraf
 *
 */
@SuppressWarnings({"restriction", "deprecation"})
public class CuteLauncherDelegate extends AbstractCLaunchDelegate {

	@Override
	protected String getPluginID() {
		return CuteCorePlugin.getUniqueIdentifier();
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if(monitor == null) {
			monitor = new NullProgressMonitor();
		}
		runLocalApplication(configuration, launch, monitor);
	}
	
	

	private void runLocalApplication( ILaunchConfiguration config, ILaunch launch, IProgressMonitor monitor ) throws CoreException {
		
		monitor.beginTask( LaunchMessages.getString( "LocalCDILaunchDelegate.0" ), 10 ); //$NON-NLS-1$
		if ( monitor.isCanceled() ) {
			return;
		}
		monitor.worked( 1 );
		try {
			IPath exePath=verifyProgramPath( config );
			IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
			IFile exeFile = wsRoot.getFile(exePath.makeRelativeTo(wsRoot.getRawLocation()));
			cleanGcdaFilesInRefedProjects(exeFile.getProject());
			File wd = getWorkingDirectory( config );
			if ( wd == null ) {
				wd = new File( System.getProperty( "user.home", "." ) ); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			String arguments[] = getProgramArgumentsArray( config );
			ArrayList<String> command = new ArrayList<String>( 1 + arguments.length );
			command.add( exePath.toOSString() );
			command.addAll( Arrays.asList( arguments ) );
			String[] commandArray = command.toArray( new String[command.size()] );
			boolean usePty = config.getAttribute( "ch.hsr.ifs.cutelauncher.useTerminal", true); //$NON-NLS-1$
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
				
				ConsoleLinkHandler handler = new ConsoleLinkHandler(exePath, textCons);
				ModellBuilder modelHandler = new ModellBuilder(exePath, launch);
				ConsolePatternListener listener = new ConsolePatternListener(getConsoleEventParser());
				listener.addHandler(handler);
				listener.addHandler(modelHandler);
				textCons.addPatternMatchListener(listener);
			}
			updateGcov(exeFile.getProject());
			for(IProject refProj :exeFile.getProject().getReferencedProjects()) {
				updateGcov(refProj);
			}
			
		}
		finally {
			monitor.done();
		}		
	}

	private void cleanGcdaFilesInRefedProjects(IProject project) throws CoreException {
		for(IProject refProj : project.getReferencedProjects()) {
			cleanGcdaFilesinProject(refProj);
		}
		
	}

	private void cleanGcdaFilesinProject(IProject refProj) throws CoreException {
		refProj.accept(new IResourceVisitor() {
			
			public boolean visit(IResource resource) throws CoreException {
				if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					String fileExtension = file.getFileExtension();
					if(fileExtension != null && fileExtension.equals("gcda")) {
						file.delete(true, new NullProgressMonitor());
					}
				}
				return true;
			}
		});
		
	}

	private void updateGcov(IProject iProject) {
		ICProjectDescription desc = CCorePlugin.getDefault().getProjectDescription(iProject);
		ICSourceEntry[] sourceEntries = desc.getActiveConfiguration().getSourceEntries();
		List<ICSourceEntry> sourceEntriesList = new ArrayList<ICSourceEntry>();
		for (ICSourceEntry icSourceEntry : sourceEntries) {
			if(!icSourceEntry.getLocation().lastSegment().equals("cute")) {
				sourceEntriesList.add(icSourceEntry);
			}
		}
		try {
			iProject.accept(new SourceFileVisitor(sourceEntriesList));
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	private class SourceFileVisitor implements IResourceVisitor{
		
		class ParseJob extends Job{

			private IFile file;
			private LineCoverageParser parser = new ModelBuilderLineParser();

			public ParseJob(IFile file) {
				super("gcov parse " + file.getName());
				this.file = file;
			}

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				parser.parse(file);
				return new Status(IStatus.OK, GcovPlugin.PLUGIN_ID, "OK");
			}
			
		}
		

		private List<ICSourceEntry> sourceEntries;

		public SourceFileVisitor(List<ICSourceEntry> sourceEntriesList) {
			this.sourceEntries = sourceEntriesList;
		}

		public boolean visit(IResource resource) throws CoreException {
			for (ICSourceEntry sourceEntry : sourceEntries) {
				if(sourceEntry.getLocation().isPrefixOf(resource.getLocation()) && isNotInExclusion(sourceEntry, resource)) {
					if (resource instanceof IFile) {
						IFile file = (IFile) resource;
						if(file.getName().endsWith("cpp")) {
							parse(file);
						}
					}
				}
			}
			return true;
		}

		protected void parse(final IFile file) throws CoreException {
			ParseJob job = new ParseJob(file);
			job.schedule();
			
		}

		private boolean isNotInExclusion(ICSourceEntry sourceEntry, IResource resource) {
			for (IPath exPath : sourceEntry.getExclusionPatterns()) {
				if(sourceEntry.getLocation().append(exPath).isPrefixOf(resource.getLocation())) {
					return false;
				}
			}
			return true;
		}
		}

	protected ConsoleEventParser getConsoleEventParser() {
		return new CuteConsoleEventParser();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected String[] getEnvironment(ILaunchConfiguration config) throws CoreException{
		Map map = config.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map)null);
		if(map==null)return super.getEnvironment( config );
		
		String[] array = DebugPlugin.getDefault().getLaunchManager().getEnvironment(config);
		if (array == null) {
			return new String[0];
		}
		return array;
		
	}
	
	public IPath sourcelookupPath(ILaunchConfiguration config, IPath exePath){
		try{
		if(config!=null && config.getAttribute(CustomisedLaunchConfigTab.USE_CUSTOM_SRC_PATH, false)){
			String rootpath=org.eclipse.core.runtime.Platform.getLocation().toOSString();
			String customSrcPath=config.getAttribute(CustomisedLaunchConfigTab.CUSTOM_SRC_PATH,""); //$NON-NLS-1$
			String fileSeparator=System.getProperty("file.separator"); //$NON-NLS-1$
			return new org.eclipse.core.runtime.Path(rootpath+customSrcPath+fileSeparator);
		}else{
			return exePath.removeLastSegments(1);
		}}catch(CoreException ce){CuteCorePlugin.getDefault().getLog().log(ce.getStatus());}
		return exePath;//on error, log and make no changes
 	}

	protected Process exec( String[] cmdLine, String[] environ, File workingDirectory, boolean usePty ) throws CoreException {
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
			if ( p != null ) {
				p.destroy();
			}
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
