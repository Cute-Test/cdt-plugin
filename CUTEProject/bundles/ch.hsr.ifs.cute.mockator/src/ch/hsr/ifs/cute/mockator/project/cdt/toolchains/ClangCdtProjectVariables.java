package ch.hsr.ifs.cute.mockator.project.cdt.toolchains;

import java.util.Collection;


public class ClangCdtProjectVariables implements ToolChainProjectVariables {

   @Override
   public String getCppCompilerToolId() {
      throw new IllegalArgumentException("Not implemented");
   }

   @Override
   public String getCppCompilerOtherFlagsId() {
      throw new IllegalArgumentException("Not implemented");
   }

   @Override
   public Collection<String> getLinkerToolIds() {
      throw new IllegalArgumentException("Not implemented");
   }

   @Override
   public String getLinkerOtherFlags() {
      throw new IllegalArgumentException("Not implemented");
   }

   @Override
   public String getPreprocessorDefinesId() {
      throw new IllegalArgumentException("Not implemented");
   }

   @Override
   public String getCpp11ExperimentalFlag() {
      throw new IllegalArgumentException("Not implemented");
   }

   @Override
   public String getCompilerPicId() {
      throw new IllegalArgumentException("Not implemented");
   }
}
