package ch.hsr.ifs.cute.headers.utils;

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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import ch.hsr.ifs.cute.headers.CuteHeadersPlugin;
import ch.hsr.ifs.cute.headers.Messages;

public class CopyUtils {

	public static void copyHeaderFiles(IContainer container, IProgressMonitor monitor, String versionNumber) throws CoreException {
		copyFilesToFolder(container, monitor, getHeaderFiles(versionNumber));
	}

	public static void copyTestFiles(IContainer container, IProgressMonitor monitor, String versionNumber) throws CoreException {
		copyFilesToFolder(container, monitor, getTestFiles(versionNumber));
	}

	private static List<URL> getHeaderFiles(String versionNumber) {
		return getFileListe("headers", "*.*", versionNumber);
	}

	private static List<URL> getTestFiles(String versionNumber) {
		return getFileListe("newCuteProject", "*.*", versionNumber);
	}

	private static void copyFilesToFolder(IContainer container, IProgressMonitor monitor, List<URL> urls) throws CoreException {
		SubMonitor mon = SubMonitor.convert(monitor, urls.size());
		for (URL url : urls) {
			String[] elements = url.getFile().split("/");
			String filename = elements[elements.length - 1];
			mon.subTask(Messages.CuteHeaders_copy + filename);
			IFile targetFile = container.getFile(new Path(filename));
			try {
				targetFile.create(url.openStream(), IResource.FORCE, new SubProgressMonitor(monitor, 1));
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, CuteHeadersPlugin.PLUGIN_ID, 42, e.getMessage(), e));
			}
			mon.worked(1);
			mon.done();
		}
	}

	private static List<URL> getFileListe(String subPath, String filePattern, String versionNumber) {
		String path = getFolderPath(subPath, versionNumber);
		Enumeration<URL> en = CuteHeadersPlugin.getDefault().getBundle().findEntries(path, filePattern, false);
		List<URL> list = new ArrayList<URL>();
		while (en.hasMoreElements()) {
			list.add(en.nextElement());
		}
		return list;
	}

	private static String getFolderPath(String subPath, String versionNumber) {
		return "cuteHeaders/" + versionNumber + "/" + subPath;
	}

	public static void copySuiteFiles(IContainer container, IProgressMonitor mon, String suitename, boolean copyTestCPP, String version) throws CoreException {
		SubMonitor subMonitor;
		if (copyTestCPP) {
			subMonitor = SubMonitor.convert(mon, 3);
			subMonitor.subTask(Messages.CuteHeaders_copyTestCPP);
			copySuiteFile(container, mon, "Test.cpp", "Test.cpp", suitename, version);
			subMonitor.worked(1);
		} else {
			subMonitor = SubMonitor.convert(mon, 2);
		}
		subMonitor.subTask(Messages.CuteHeaders_copySuite);
		copySuiteFile(container, mon, "$suitename$.cpp", suitename + ".cpp", suitename, version);
		subMonitor.worked(1);
		copySuiteFile(container, mon, "$suitename$.h", suitename + ".h", suitename, version);
		subMonitor.worked(1);
		subMonitor.done();
	}

	private static void copySuiteFile(IFile targetFile, IProgressMonitor mon, String templateFilename, String suitename, String version) throws CoreException {
		Enumeration<URL> en = CuteHeadersPlugin.getDefault().getBundle().findEntries(getFolderPath("newCuteSuite", version), templateFilename, false);
		if (en.hasMoreElements()) {
			URL url = en.nextElement();
			try {
				ByteArrayInputStream str = implantActualSuiteName(url, suitename);
				targetFile.create(str, IResource.FORCE, new SubProgressMonitor(mon, 1));
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, CuteHeadersPlugin.PLUGIN_ID, 42, e.getMessage(), e));
			}
		} else {
			throw new CoreException(new Status(IStatus.ERROR, CuteHeadersPlugin.PLUGIN_ID, 42, "missing suite template files", null));
		}
	}

	private static void copySuiteFile(IContainer container, IProgressMonitor monitor, String templateFilename, String targetFilename, String suitename,
			String versionNumber) throws CoreException {
		IFile targetFile = container.getFile(new Path(targetFilename));
		copySuiteFile(targetFile, monitor, templateFilename, suitename, versionNumber);
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
