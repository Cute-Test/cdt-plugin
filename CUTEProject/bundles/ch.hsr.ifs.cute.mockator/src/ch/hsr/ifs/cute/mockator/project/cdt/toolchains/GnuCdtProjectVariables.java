package ch.hsr.ifs.cute.mockator.project.cdt.toolchains;

import static ch.hsr.ifs.iltis.core.collections.CollectionUtil.list;

import java.util.Collection;


public class GnuCdtProjectVariables implements ToolChainProjectVariables {

    @Override
    public String getCppCompilerToolId() {
        return "cdt.managedbuild.tool.gnu.cpp.compiler";
    }

    @Override
    public String getCppCompilerOtherFlagsId() {
        return "gnu.cpp.compiler.option.other.other";
    }

    @Override
    public Collection<String> getLinkerToolIds() {
        // GCC under Mac OS X uses its own linker
        return list("cdt.managedbuild.tool.gnu.cpp.linker", "cdt.managedbuild.tool.macosx.cpp.linker");
    }

    @Override
    public String getLinkerOtherFlags() {
        return "gnu.cpp.link.option.other";
    }

    @Override
    public String getPreprocessorDefinesId() {
        return "gnu.cpp.compiler.option.preprocessor.def";
    }

    @Override
    public String getCpp11ExperimentalFlag() {
        return "-std=c++0x";
    }

    @Override
    public String getCompilerPicId() {
        return "gnu.cpp.compiler.option.other.pic";
    }
}
