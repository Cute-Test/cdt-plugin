package ch.hsr.ifs.cute.mockator.tests.base.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.ifs.iltis.core.core.resources.IOUtil;

import ch.hsr.ifs.cute.mockator.base.util.PathProposalUtil;


public class PathProposalUtilTest {

   private IProject project;

   @Before
   public void setUp() throws CoreException {
      final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      project = root.getProject("Test");
      project.create(new NullProgressMonitor());
      project.open(new NullProgressMonitor());
   }

   @After
   public void tearDown() throws CoreException {
      project.delete(true, true, new NullProgressMonitor());
   }

   @Test
   public void fileAlreadyExistsYieldsAlternative() throws CoreException {
      final IFile file = project.getFile("test.cpp");
      file.create(IOUtil.StringIO.read("test"), true, new NullProgressMonitor());
      final PathProposalUtil util = new PathProposalUtil(project.getFullPath());
      final IPath uniquePath = util.getUniquePathForNewFile("test", ".cpp");
      Assert.assertEquals(new Path("/Test/test1.cpp"), uniquePath);
   }

   @Test
   public void fileNotExistsYieldsOriginal() {
      final PathProposalUtil util = new PathProposalUtil(project.getFullPath());
      final IPath uniquePath = util.getUniquePathForNewFile("test", ".cpp");
      Assert.assertEquals(new Path("/Test/test.cpp"), uniquePath);
   }
}
