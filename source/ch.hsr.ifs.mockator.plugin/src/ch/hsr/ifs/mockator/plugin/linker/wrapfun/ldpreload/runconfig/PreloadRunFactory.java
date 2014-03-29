package ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload.runconfig;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;

import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.project.cdt.toolchains.ToolChain;

class PreloadRunFactory {

  public Maybe<PreloadRunStrategy> getRunConfig(IProject project) {
    PreloadRunStrategy strategy = null;

    for (ToolChain optTc : ToolChain.fromProject(project)) {
      switch (optTc) {
        case GnuLinux:
          strategy = new LinuxPreloadRunConfig();
          break;
        case GnuMacOSX:
          strategy = new MacOsXPreloadRunConfig();
          break;
        case GnuCygWin:
        case GnuMinGw:
        case ClangLinux:
          break;
        default:
          break;
      }
    }
    return maybe(strategy);
  }
}
