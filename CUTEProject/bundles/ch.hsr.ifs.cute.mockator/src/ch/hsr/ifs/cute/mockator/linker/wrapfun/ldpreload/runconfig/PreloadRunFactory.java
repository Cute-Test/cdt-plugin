package ch.hsr.ifs.cute.mockator.linker.wrapfun.ldpreload.runconfig;

import java.util.Optional;

import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.cute.mockator.project.cdt.toolchains.ToolChain;


class PreloadRunFactory {

    public Optional<PreloadRunStrategy> getRunConfig(final IProject project) {
        return ToolChain.fromProject(project).map(tc -> {
            switch (tc) {
            case GnuLinux:
                return new LinuxPreloadRunConfig();
            case GnuMacOSX:
                return new MacOsXPreloadRunConfig();
            case GnuCygWin:
            case GnuMinGw:
            case ClangLinux:
            default:
                return null;
            }
        });
    }
}
