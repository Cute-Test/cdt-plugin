package ch.hsr.ifs.cute.core.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.osgi.framework.Bundle;

import ch.hsr.ifs.cute.core.CuteCorePlugin;

public class CopyUtils {

	public static void copyHeaderFiles(Bundle bundle, IContainer container, IProgressMonitor monitor, String versionNumber) throws CoreException {
		copyFilesToFolder(container, monitor, getHeaderFiles(bundle, versionNumber));
	}

	public static void copyTestFiles(Bundle bundle, IContainer container, IProgressMonitor monitor, String versionNumber) throws CoreException {
		IProject project = container.getProject();
		copyFilesToFolder(container, monitor, getTestFiles(bundle, versionNumber, project));
	}

	private static List<URL> getHeaderFiles(Bundle bundle, String versionNumber) {
		return getFileList(bundle, "headers", "*.*", versionNumber);
	}

	private static String getCpp11Appendix(IProject project) {
		return VersionQuery.isCPPVersionAboveOrEqualEleven(project) ? "_11plus" : "";
	}

	private static List<URL> getTestFiles(Bundle bundle, String versionNumber, IProject project) {
		return getFileList(bundle, "newCuteProject" + getCpp11Appendix(project), "*.*", versionNumber);
	}

	private static void copyFilesToFolder(IContainer container, IProgressMonitor monitor, List<URL> urls) throws CoreException {
		SubMonitor mon = SubMonitor.convert(monitor, urls.size());
		for (URL url : urls) {
			String[] elements = url.getFile().split("/");
			String filename = elements[elements.length - 1];
			mon.subTask("Copy " + filename);
			IFile targetFile = container.getFile(new Path(filename));
			try {
				targetFile.create(url.openStream(), IResource.FORCE, SubMonitor.convert(monitor, 1));
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, CuteCorePlugin.PLUGIN_ID, 42, e.getMessage(), e));
			}
			mon.worked(1);
			mon.done();
		}
	}

	private static List<URL> getFileList(Bundle bundle, String subPath, String filePattern, String versionNumber) {
		String path = getFolderPath(subPath, versionNumber);
		Enumeration<URL> en = bundle.findEntries(path, filePattern, false);
		List<URL> list = new ArrayList<URL>();
		while (en.hasMoreElements()) {
			list.add(en.nextElement());
		}
		return list;
	}

	private static String getFolderPath(String subPath, String versionNumber) {
		return "cuteHeaders/" + versionNumber + "/" + subPath;
	}

	public static void copySuiteFiles(Bundle bundle, IContainer container, IProgressMonitor mon, String suitename, boolean copyTestCPP, String version) throws CoreException {
		SubMonitor subMonitor;
		if (copyTestCPP) {
			subMonitor = SubMonitor.convert(mon, 3);
			subMonitor.subTask("Copy Test.cpp");
			copySuiteFile(bundle, container, mon, "Test.cpp", "Test.cpp", suitename, version);
			subMonitor.worked(1);
		} else {
			subMonitor = SubMonitor.convert(mon, 2);
		}
		subMonitor.subTask("Copy Suite");
		copySuiteFile(bundle, container, mon, "$suitename$.cpp", suitename + ".cpp", suitename, version);
		subMonitor.worked(1);
		copySuiteFile(bundle, container, mon, "$suitename$.h", suitename + ".h", suitename, version);
		subMonitor.worked(1);
		subMonitor.done();
	}

	private static void copySuiteFile(Bundle bundle, IFile targetFile, IProgressMonitor mon, String templateFilename, String suitename, String version) throws CoreException {
		String subfolder = "newCuteSuite" + getCpp11Appendix(targetFile.getProject());
		Enumeration<URL> en = bundle.findEntries(getFolderPath(subfolder, version), templateFilename, false);
		if (en.hasMoreElements()) {
			URL url = en.nextElement();
			try {
				ByteArrayInputStream str = implantActualSuiteName(url, suitename);
				targetFile.create(str, IResource.FORCE, SubMonitor.convert(mon, 1));
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, CuteCorePlugin.PLUGIN_ID, 42, e.getMessage(), e));
			}
		} else {
			throw new CoreException(new Status(IStatus.ERROR, CuteCorePlugin.PLUGIN_ID, 42, "missing suite template files", null));
		}
	}

	private static void copySuiteFile(Bundle bundle, IContainer container, IProgressMonitor monitor, String templateFilename, String targetFilename, String suitename,
			String versionNumber) throws CoreException {
		IFile targetFile = container.getFile(new Path(targetFilename));
		copySuiteFile(bundle, targetFile, monitor, templateFilename, suitename, versionNumber);
	}

	private static ByteArrayInputStream implantActualSuiteName(URL url, String suitename) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		StringBuilder buffer = new StringBuilder();
		String linesep = System.getProperty("line.separator");
		while (br.ready()) {
			String a = br.readLine();
			if(a.contains("$suitename$")) {
				buffer.append(a.replaceAll("[$]suitename[$]", suitename) + linesep);
			} else {
				buffer.append(a.replaceAll("[$]SUITENAME[$]", suitename.toUpperCase()) + linesep);
			}
		}
		br.close();
		return new ByteArrayInputStream(buffer.toString().getBytes());
	}
}
