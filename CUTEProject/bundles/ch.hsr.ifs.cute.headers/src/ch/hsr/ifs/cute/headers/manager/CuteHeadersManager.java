package ch.hsr.ifs.cute.headers.manager;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;

import ch.hsr.ifs.iltis.cpp.versionator.definition.CPPVersion;

import ch.hsr.ifs.cute.core.CuteCorePlugin;
import ch.hsr.ifs.cute.headers.CuteHeadersPlugin;
import ch.hsr.ifs.cute.headers.ICuteHeaders;
import ch.hsr.ifs.cute.headers.utils.VersionQuery;


public class CuteHeadersManager {

   private CuteHeadersManager() {}

   public static final String PLUGIN_ID = "ch.hsr.ifs.cute.ui";

   public static final QualifiedName CUTE_VERSION_PROPERTY_NAME = new QualifiedName(PLUGIN_ID, "cuteVersion");

   public static ICuteHeaders getCuteVersion(IProject project) throws CoreException {
      String versionString = project.getPersistentProperty(CUTE_VERSION_PROPERTY_NAME);
      if (versionString != null) {
         return ICuteHeaders.loadHeadersForVersionNumber(versionString);
      } else { // find out version by parsing the version header
         IResource res = project.findMember("cute/cute_version.h");
         if (res instanceof IFile) {
            String cuteVersionstring = "";
            IFile file = (IFile) res;
            BufferedReader br = new BufferedReader(new InputStreamReader(file.getContents()));
            String line;
            try {
               Pattern versPtr = Pattern.compile("#define CUTE_LIB_VERSION \\\"(\\d\\.\\d\\.\\d)\\\"$");
               while ((line = br.readLine()) != null) {
                  Matcher matcher = versPtr.matcher(line);
                  if (matcher.matches()) {
                     cuteVersionstring = "CUTE Headers " + matcher.group(1);
                     project.setPersistentProperty(CUTE_VERSION_PROPERTY_NAME, cuteVersionstring);
                     break;
                  }
               }
               return ICuteHeaders.loadHeadersForVersionNumber(cuteVersionstring);
            } catch (IOException e) {} finally {
               try {
                  br.close();
               } catch (IOException e) {}
            }
         }
         return ICuteHeaders.getDefaultHeaders(CPPVersion.getForProject(project));
      }
   }

   public static void setCuteVersion(IProject project, ICuteHeaders headers) throws CoreException {
      project.setPersistentProperty(CUTE_VERSION_PROPERTY_NAME, headers.getVersionString());
   }

   public static void replaceCuteHeaders(IProject project, ICuteHeaders headers, IProgressMonitor monitor) {
      IFolder cuteFolder = project.getFolder("cute");
      try {
         if (!cuteFolder.exists()) {
            cuteFolder.create(true, true, monitor);
         }
         IResource[] files = cuteFolder.members();
         SubMonitor mon = SubMonitor.convert(monitor, files.length * 2);
         for (IResource resource : files) {
            mon.subTask(NLS.bind("Remove {0}", resource.getName()));
            resource.delete(true, new NullProgressMonitor());
            mon.worked(1);
         }
         headers.copyHeaderFiles(cuteFolder, mon.newChild(files.length));
         ICuteHeaders.setForProject(project, headers);
      } catch (CoreException e) {
         CuteHeadersPlugin.log("Exception while replacing headers", e);
      }
   }

   public static void copyHeaderFiles(Bundle bundle, IContainer container, IProgressMonitor monitor, String versionNumber) throws CoreException {
      CuteHeadersManager.copyFilesToFolder(container, monitor, CuteHeadersManager.getHeaderFiles(bundle, versionNumber));
   }

   public static void copyTestFiles(Bundle bundle, IContainer container, IProgressMonitor monitor, String versionNumber) throws CoreException {
      IProject project = container.getProject();
      CuteHeadersManager.copyFilesToFolder(container, monitor, CuteHeadersManager.getTestFiles(bundle, versionNumber, project));
   }

   public static List<URL> getHeaderFiles(Bundle bundle, String versionNumber) {
      return CuteHeadersManager.getFileList(bundle, "headers", "*.*", versionNumber);
   }

   public static String getCpp11Appendix(IProject project) {
      return VersionQuery.isCPPVersionAboveOrEqualEleven(project) ? "_11plus" : "";
   }

   public static List<URL> getTestFiles(Bundle bundle, String versionNumber, IProject project) {
      return CuteHeadersManager.getFileList(bundle, "newCuteProject" + getCpp11Appendix(project), "*.*", versionNumber);
   }

   public static void copyFilesToFolder(IContainer container, IProgressMonitor monitor, List<URL> urls) throws CoreException {
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

   public static List<URL> getFileList(Bundle bundle, String subPath, String filePattern, String versionNumber) {
      String path = getFolderPath(subPath, versionNumber);
      Enumeration<URL> en = bundle.findEntries(path, filePattern, false);
      List<URL> list = new ArrayList<>();
      while (en.hasMoreElements()) {
         list.add(en.nextElement());
      }
      return list;
   }

   public static String getFolderPath(String subPath, String versionNumber) {
      return "cuteHeaders/" + versionNumber + "/" + subPath;
   }

   public static void copySuiteFiles(Bundle bundle, IContainer container, IProgressMonitor mon, String suitename, boolean copyTestCPP, String version)
         throws CoreException {
      SubMonitor subMonitor;
      if (copyTestCPP) {
         subMonitor = SubMonitor.convert(mon, 3);
         subMonitor.subTask("Copy Test.cpp");
         CuteHeadersManager.copySuiteFile(bundle, container, mon, "Test.cpp", "Test.cpp", suitename, version);
         subMonitor.worked(1);
      } else {
         subMonitor = SubMonitor.convert(mon, 2);
      }
      subMonitor.subTask("Copy Suite");
      CuteHeadersManager.copySuiteFile(bundle, container, mon, "$suitename$.cpp", suitename + ".cpp", suitename, version);
      subMonitor.worked(1);
      CuteHeadersManager.copySuiteFile(bundle, container, mon, "$suitename$.h", suitename + ".h", suitename, version);
      subMonitor.worked(1);
      subMonitor.done();
   }

   public static void copySuiteFile(Bundle bundle, IFile targetFile, IProgressMonitor mon, String templateFilename, String suitename, String version)
         throws CoreException {
      String subfolder = "newCuteSuite" + getCpp11Appendix(targetFile.getProject());
      Enumeration<URL> en = bundle.findEntries(getFolderPath(subfolder, version), templateFilename, false);
      if (en.hasMoreElements()) {
         URL url = en.nextElement();
         try {
            ByteArrayInputStream str = CuteHeadersManager.implantActualSuiteName(url, suitename);
            targetFile.create(str, IResource.FORCE, SubMonitor.convert(mon, 1));
         } catch (IOException e) {
            throw new CoreException(new Status(IStatus.ERROR, CuteCorePlugin.PLUGIN_ID, 42, e.getMessage(), e));
         }
      } else {
         throw new CoreException(new Status(IStatus.ERROR, CuteCorePlugin.PLUGIN_ID, 42, "missing suite template files", null));
      }
   }

   public static void copySuiteFile(Bundle bundle, IContainer container, IProgressMonitor monitor, String templateFilename, String targetFilename,
         String suitename, String versionNumber) throws CoreException {
      IFile targetFile = container.getFile(new Path(targetFilename));
      copySuiteFile(bundle, targetFile, monitor, templateFilename, suitename, versionNumber);
   }

   public static ByteArrayInputStream implantActualSuiteName(URL url, String suitename) throws IOException {
      BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
      StringBuilder buffer = new StringBuilder();
      String linesep = System.getProperty("line.separator");
      while (br.ready()) {
         String a = br.readLine();
         if (a.contains("$suitename$")) {
            buffer.append(a.replaceAll("[$]suitename[$]", suitename) + linesep);
         } else {
            buffer.append(a.replaceAll("[$]SUITENAME[$]", suitename.toUpperCase()) + linesep);
         }
      }
      br.close();
      return new ByteArrayInputStream(buffer.toString().getBytes());
   }
}
