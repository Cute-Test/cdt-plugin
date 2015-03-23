package ch.hsr.ifs.mockator.tests.project.cdt;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.ifs.mockator.plugin.project.cdt.options.ProjRelPathGenerator;

public class ProjRelPathGeneratorTest {
  private IProject project;

  @Before
  public void setUp() {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    project = root.getProject("Test");
  }

  @Test
  public void retrieveProjectRelFile() {
    IFile file = project.getFile(new Path("mockator/mockator.h"));
    String expected =
        String.format("${workspace_loc:%s${ProjName}%smockator/mockator.h}", File.separator,
            File.separator);
    assertEquals(expected, ProjRelPathGenerator.getProjectRelativePath(file));
  }

  @Test
  public void retrieveProjectRelFolder() {
    IFolder folder = project.getFolder(new Path("mockator"));
    String expected =
        String.format("${workspace_loc:%s${ProjName}%smockator}", File.separator, File.separator);
    assertEquals(expected, ProjRelPathGenerator.getProjectRelativePath(folder));
  }
}
