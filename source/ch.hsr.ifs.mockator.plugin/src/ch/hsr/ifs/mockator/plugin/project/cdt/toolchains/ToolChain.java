package ch.hsr.ifs.mockator.plugin.project.cdt.toolchains;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import java.util.Collection;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.project.cdt.CdtHelper;

public enum ToolChain {

  GnuMacOSX {
    @Override
    public String getSharedLibProjectType() {
      return "cdt.managedbuild.target.macosx.so";
    }

    @Override
    public ToolChainProjectVariables getCdtProjectVariables() {
      return new GnuCdtProjectVariables();
    }
  },
  GnuLinux {
    @Override
    public String getSharedLibProjectType() {
      return "cdt.managedbuild.target.gnu.so";
    }

    @Override
    public ToolChainProjectVariables getCdtProjectVariables() {
      return new GnuCdtProjectVariables();
    }
  },
  ClangLinux {
    @Override
    public String getSharedLibProjectType() {
      return "cdt.managedbuild.target.clang.so";
    }

    @Override
    public ToolChainProjectVariables getCdtProjectVariables() {
      return new ClangCdtProjectVariables();
    }
  },
  GnuCygWin {
    @Override
    public String getSharedLibProjectType() {
      return "cdt.managedbuild.target.gnu.cygwin.so";
    }

    @Override
    public ToolChainProjectVariables getCdtProjectVariables() {
      return new GnuCdtProjectVariables();
    }
  },
  GnuMinGw {
    @Override
    public String getSharedLibProjectType() {
      return "cdt.managedbuild.target.gnu.mingw.so";
    }

    @Override
    public ToolChainProjectVariables getCdtProjectVariables() {
      return new GnuCdtProjectVariables();
    }
  };

  public abstract String getSharedLibProjectType();

  public abstract ToolChainProjectVariables getCdtProjectVariables();

  public static Maybe<ToolChain> fromProject(IProject project) {
    IConfiguration configuration = getDefaultConfiguration(project);
    IToolChain tc = getSuperToolChain(configuration.getToolChain());
    String tcId = tc.getId();

    if (tcId.equals("cdt.managedbuild.toolchain.gnu.base") && list(tc.getOSList()).contains("linux")) //$NON-NLS-2$
      return maybe(GnuLinux);
    else if (tcId.equals("cdt.managedbuild.toolchain.gnu.macosx.base"))
      return maybe(GnuMacOSX);
    else if (tcId.equals("cdt.managedbuild.toolchain.gnu.cygwin.base"))
      return maybe(GnuCygWin);
    else if (tcId.equals("cdt.managedbuild.toolchain.gnu.mingw.base"))
      return maybe(GnuMinGw);

    return none();
  }

  public static IToolChain getSuperToolChain(IToolChain tc) {
    IToolChain currentTc = tc;

    while (currentTc.getSuperClass() != null) {
      currentTc = currentTc.getSuperClass();
    }

    return currentTc;
  }

  public static boolean hasLinkerForToolChain(IProject project, ToolChain toolChain) {
    IConfiguration config = getDefaultConfiguration(project);
    Collection<String> linkerToolIds = toolChain.getCdtProjectVariables().getLinkerToolIds();

    for (ITool tool : config.getToolChain().getTools()) {
      if (linkerToolIds.contains(CdtHelper.getSuperTool(tool).getId()))
        return true;
    }

    return false;
  }

  private static IConfiguration getDefaultConfiguration(IProject project) {
    return CdtHelper.getManagedBuildInfo(project).getDefaultConfiguration();
  }
}
