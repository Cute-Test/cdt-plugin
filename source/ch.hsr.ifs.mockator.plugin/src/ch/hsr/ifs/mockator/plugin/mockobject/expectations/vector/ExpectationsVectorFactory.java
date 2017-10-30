package ch.hsr.ifs.mockator.plugin.mockobject.expectations.vector;

import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStdFactory;


public class ExpectationsVectorFactory {

   private final CppStandard cppStd;

   public ExpectationsVectorFactory(CppStandard cppStd) {
      this.cppStd = cppStd;
   }

   public ExpectationsCppStdStrategy getStrategy() {
      return CppStdFactory.from(ExpectationsCpp03Strategy.class, ExpectationsCpp11Strategy.class).getHandler(cppStd);
   }
}
