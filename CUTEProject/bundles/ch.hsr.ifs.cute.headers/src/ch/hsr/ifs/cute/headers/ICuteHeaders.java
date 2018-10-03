package ch.hsr.ifs.cute.headers;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import ch.hsr.ifs.iltis.cpp.versionator.definition.CPPVersion;

import ch.hsr.ifs.cute.core.headers.CuteVersionNumber;
import ch.hsr.ifs.cute.headers.manager.CuteHeadersManager;
import ch.hsr.ifs.cute.headers.versions.CuteHeaders2;


/**
 *
 * @author tstauber
 *
 */
public interface ICuteHeaders {

    static String VERSION_DELIMITER = ".";

    /**
     * The registered cute headers loaders. If a new header enum is introduced, it should be added here.
     */
    static List<Supplier<ICuteHeaders[]>> headerLoaders = Arrays.asList(CuteHeaders2::values);

    public CuteVersionNumber getVersionNumber();

    public boolean compatibleWith(CPPVersion cppVersion);

    /**
     * Checks if this header is of version {@code major.minor.patch}
     */
    public default boolean isVersion(CuteVersionNumber version) {
        return compareVersion(new int[] { version.major(), version.minor(), version.patch() }) == 0;
    }

    /**
     * Returns a version-number string in the format {@code major.minor.patch}.
     */
    public default String getVersionNumberString() {
        CuteVersionNumber vers = getVersionNumber();
        return vers.major() + VERSION_DELIMITER + vers.minor() + VERSION_DELIMITER + vers.patch();
    }

    public default String getVersionString() {
        return "CUTE Headers " + getVersionNumberString();
    }

    public default void copyHeaderFiles(IContainer container, IProgressMonitor monitor) throws CoreException {
        CuteHeadersManager.copyHeaderFiles(CuteHeadersPlugin.getDefault().getBundle(), container, monitor, getVersionNumberString());
    }

    public default void copySuiteFiles(IContainer container, IProgressMonitor monitor, String suitename, boolean copyTestCPP) throws CoreException {
        CuteHeadersManager.copySuiteFiles(CuteHeadersPlugin.getDefault().getBundle(), container, monitor, suitename, copyTestCPP,
                getVersionNumberString());
    }

    public default void copyExampleTestFiles(IContainer container, IProgressMonitor monitor) throws CoreException {
        CuteHeadersManager.copyTestFiles(CuteHeadersPlugin.getDefault().getBundle(), container, monitor, getVersionNumberString());
    }

    /**
     * Used by implementing enum classes to throw if not all constants are annotated with a cute headers version.
     */
    public default void throwAnnotationMissingException(String fieldName, String classname) throws RuntimeException {
        throw new RuntimeException("Missing CuteVersionNumber annotation on field '" + fieldName + " in " + classname + "'.");
    }

    /**
     * Returns a list of all registered header versions
     */
    public static TreeSet<ICuteHeaders> loadedHeaders() {
        TreeSet<ICuteHeaders> set = new TreeSet<>(ICuteHeaders::compareVersion);
        set.addAll(headerLoaders.stream().flatMap(loader -> Stream.of(loader.get())).collect(Collectors.toSet()));
        return set;
    }

    public default int compareVersion(ICuteHeaders other) {
        CuteVersionNumber otherVers = other.getVersionNumber();
        return compareVersion(new int[] { otherVers.major(), otherVers.minor(), otherVers.patch() });
    }

    public default int compareVersion(int[] other) {
        if (other.length != 3) throw new IllegalArgumentException("The version-array must provide three values (major, minor, patch)");
        CuteVersionNumber vers = getVersionNumber();
        if (vers.major() - other[0] != 0) return vers.major() - other[0];
        if (vers.minor() - other[1] != 0) return vers.minor() - other[1];
        return vers.patch() - other[2];
    }

    /**
     * This uses the registered header loaders to load the headers with the specified version.
     *
     * @param versionNumber
     * The version-number string. Must be in the format provided
     * by {@link #getVersionNumber()}
     *
     * @return The {@code CuteHeadersVersion} if any available or else an Optional.empty
     */
    public static ICuteHeaders loadHeadersForVersionNumber(String versionNumber) {
        for (Supplier<ICuteHeaders[]> loader : headerLoaders) {
            ICuteHeaders[] versions = loader.get();
            for (ICuteHeaders version : versions) {
                if (version.getVersionString().equals(versionNumber)) return version;
            }
        }
        return null;
    }

    public static ICuteHeaders getDefaultHeaders(CPPVersion cppVersion) {
        // TODO(tstauber - Apr 16, 2018) Limit headers versions (see CPPVersion.setVersionRange(...))
        if (cppVersion == null) return loadedHeaders().stream().max(ICuteHeaders::compareVersion).orElse(null);
        return loadedHeaders().stream().filter(h -> h.compatibleWith(cppVersion)).max(ICuteHeaders::compareVersion).orElse(null);
    }

    public static ICuteHeaders getForProject(IProject project) throws CoreException {
        return CuteHeadersManager.getCuteVersion(project);
    }

    public static void setForProject(IProject project, ICuteHeaders headers) throws CoreException {
        CuteHeadersManager.setCuteVersion(project, headers);
    }

    public static void removeHeaderFiles(IFolder cuteFolder, NullProgressMonitor monitor) throws CoreException {
        cuteFolder.delete(true, monitor);
    }

}
