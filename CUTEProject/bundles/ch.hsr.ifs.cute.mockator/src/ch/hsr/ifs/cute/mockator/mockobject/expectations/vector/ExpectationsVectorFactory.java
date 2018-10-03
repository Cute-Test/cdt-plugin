package ch.hsr.ifs.cute.mockator.mockobject.expectations.vector;

import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.project.properties.CppStdFactory;


public class ExpectationsVectorFactory {

    private final CppStandard cppStd;

    public ExpectationsVectorFactory(final CppStandard cppStd) {
        this.cppStd = cppStd;
    }

    public ExpectationsCppStdStrategy getStrategy() {
        return CppStdFactory.from(ExpectationsCpp03Strategy.class, ExpectationsCpp11Strategy.class).getHandler(cppStd);
    }
}
