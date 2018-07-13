package ch.hsr.ifs.mockator.plugin.project.cdt.toolchains;

import java.util.Collection;


public interface ToolChainProjectVariables {

   String getCppCompilerToolId();

   String getCppCompilerOtherFlagsId();

   Collection<String> getLinkerToolIds();

   String getLinkerOtherFlags();

   String getPreprocessorDefinesId();

   String getCpp11ExperimentalFlag();

   String getCompilerPicId();
}
