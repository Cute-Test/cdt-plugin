package ch.hsr.ifs.cute.core.headers;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.framework.Bundle;

import ch.hsr.ifs.cute.core.utils.CopyUtils;


public abstract class CuteHeaders implements Comparable<CuteHeaders> {

   private final CuteVersionNumber fVersionNumber;

   public CuteHeaders() {
      fVersionNumber = getClass().getAnnotation(CuteVersionNumber.class);

      if (fVersionNumber == null) { throw new RuntimeException("Missing CuteVersionNumber annotation on '" + getClass().getName() + "'."); }
   }

   public String getVersionNumber() {
      return String.format("%d.%d.%d", fVersionNumber.major(), fVersionNumber.minor(), fVersionNumber.patch());
   }

   public String getVersionString() {
      return "CUTE Headers " + getVersionNumber();
   }

   public abstract Bundle getBundle();

   public void copyHeaderFiles(IContainer container, IProgressMonitor monitor) throws CoreException {
      CopyUtils.copyHeaderFiles(getBundle(), container, monitor, getVersionNumber());

   }

   public void copySuiteFiles(IContainer container, IProgressMonitor monitor, String suitename, boolean copyTestCPP) throws CoreException {
      CopyUtils.copySuiteFiles(getBundle(), container, monitor, suitename, copyTestCPP, getVersionNumber());
   }

   public void copyTestFiles(IContainer container, IProgressMonitor monitor) throws CoreException {
      CopyUtils.copyTestFiles(getBundle(), container, monitor, getVersionNumber());
   }

   @Override
   public int compareTo(CuteHeaders o) {
      if (fVersionNumber.major() != o.fVersionNumber.major()) { return o.fVersionNumber.major() - fVersionNumber.major(); }

      if (fVersionNumber.minor() != o.fVersionNumber.minor()) { return o.fVersionNumber.minor() - fVersionNumber.minor(); }

      return o.fVersionNumber.patch() - fVersionNumber.patch();
   }
}
