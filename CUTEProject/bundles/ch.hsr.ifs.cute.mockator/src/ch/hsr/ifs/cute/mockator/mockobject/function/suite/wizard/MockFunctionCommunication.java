package ch.hsr.ifs.cute.mockator.mockobject.function.suite.wizard;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;


public interface MockFunctionCommunication {

    void setSuiteName(String suiteName);

    void setDestinationFolder(IPath destinationPath);

    void execute(IProgressMonitor pm);

    IFile getNewFile();
}
