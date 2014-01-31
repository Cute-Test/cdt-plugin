package ch.hsr.ifs.cute.core.launch;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICOutputEntry;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

public class LaunchEnvironmentVariables {
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	public static void apply(ILaunchConfigurationWorkingCopy wc,
			ICProject project) throws CoreException{
		
		String os = Platform.getOS();
		if(os.equals(Platform.OS_WIN32))setWin32PATH(wc, project);
		if(os.equals(Platform.OS_LINUX))setLinuxLD_LIBRARY_PATH(wc, project);
		if(os.equals(Platform.OS_MACOSX))setMacDYLD_LIBRARY_PATH(wc, project);
	}
	
	
	private static void setMacDYLD_LIBRARY_PATH(ILaunchConfigurationWorkingCopy wc,
			ICProject project) throws CoreException{
		setPathEnvironmentVariable(wc,project,"DYLD_LIBRARY_PATH"); //$NON-NLS-1$
	}
	private static void setLinuxLD_LIBRARY_PATH(ILaunchConfigurationWorkingCopy wc,
			ICProject project) throws CoreException{
		setPathEnvironmentVariable(wc,project,"LD_LIBRARY_PATH"); //$NON-NLS-1$
	}
	private static void setWin32PATH(ILaunchConfigurationWorkingCopy wc,
			ICProject project) throws CoreException{
		setPathEnvironmentVariable(wc,project,"PATH"); //$NON-NLS-1$
	}
	
	//caveat:not for generic environment variable
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void setPathEnvironmentVariable(ILaunchConfigurationWorkingCopy wc,
			ICProject project,String environmentVariableName) throws CoreException {
		String path=getBuildEnvironmentVariable(environmentVariableName,project);
		String pathSeparator=System.getProperty("path.separator");//assumption that it is only 1 char wide //$NON-NLS-1$
		if(!path.equals(EMPTY_STRING))
			if( !(path.charAt(path.length()-1)+EMPTY_STRING).equals(pathSeparator))
				path+=pathSeparator;
		
		IProject[] libProject=getReferencedProjects(project);
		if(libProject.length==0)return;
		String libPath=generateLibPath(libProject,pathSeparator);

//		Map m3=project.getOptions(true);//get information abt formatter etc
//		Map m3=getBuildEnvironmentVariables(project);//get the entire build env variables
		Map map=new TreeMap();
		map.put(environmentVariableName, path+libPath);
		wc.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, map);
//		wc.setAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
	}

	private static String getBuildEnvironmentVariable(String key,ICProject project) {
		String result=EMPTY_STRING;
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project.getUnderlyingResource());
		if (info != null) {
			IConfiguration ic=info.getDefaultConfiguration();
			IEnvironmentVariableProvider evp=ManagedBuildManager.getEnvironmentVariableProvider();
			IEnvironmentVariable ev=evp.getVariable(key, ic, true);
			if(ev!=null)
				result=ev.getValue();	
		}
		return result;
	}

	private static IProject[] getReferencedProjects(ICProject project) throws CoreException {
		IProject prj=project.getProject();
		IProjectDescription desc = prj.getDescription();
		IProject ref[]=desc.getReferencedProjects();
		return ref;
	}
	
	private static String generateLibPath(IProject[] libProject, String pathSeparator) {
		if(libProject.length<1)return EMPTY_STRING;
		
		String result=EMPTY_STRING;
		
		for(int x=0;x<libProject.length;x++){
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(libProject[x]);
			if(info != null) {
				IConfiguration config = info.getDefaultConfiguration();
				//			ICSourceEntry[] sources = config.getSourceEntries();
				ICOutputEntry[]  dirs = config.getBuildData().getOutputDirectories();	
				for (ICOutputEntry outputEntry : dirs) {
					IPath location = outputEntry.getFullPath();
					IPath parameter;
					if(location.segmentCount()== 0){
						parameter=libProject[x].getFullPath();
					}else{
						parameter=libProject[x].getFolder(location).getFullPath();	
					}
					result+= "${workspace_loc:" + parameter.toPortableString() + "}"+pathSeparator; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		
		return result;
	}
	
	public static Map<String, String> getBuildEnvironmentVariables(ICProject project) {
		Map<String, String> result=new TreeMap<String, String>();
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project.getUnderlyingResource());
		if (info != null) {
			IConfiguration ic=info.getDefaultConfiguration();
			IEnvironmentVariableProvider evp=ManagedBuildManager.getEnvironmentVariableProvider();
			IEnvironmentVariable[] ev=evp.getVariables(ic, false);
			
			for(IEnvironmentVariable iev:ev){
				result.put(iev.getName(), iev.getValue());
			}
			
		}
		return result;
	}
	
}
