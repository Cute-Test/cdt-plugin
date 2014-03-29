package ch.hsr.ifs.mockator.plugin;

import ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly.StaticPolymorphismChecker;
import ch.hsr.ifs.mockator.plugin.incompleteclass.subtype.SubtypePolymorphismChecker;
import ch.hsr.ifs.mockator.plugin.linker.wrapfun.gnuoption.GnuOptionChecker;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.qf.InconsistentExpectationsChecker;
import ch.hsr.ifs.mockator.plugin.preprocessor.qf.TraceFunctionChecker;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.staticpoly.MissingTestDoubleStaticPolyChecker;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype.MissingTestDoubleSubTypeChecker;

public abstract class MockatorProblemIds {
  public static final String STATIC_POLY_MISSING_MEMFUNS_IMPL_PROBLEM_ID =
      StaticPolymorphismChecker.STATIC_POLY_MISSING_MEMFUNS_IMPL_PROBLEM_ID;

  public static final String SUBTYPE_MISSING_MEMFUNS_IMPL_PROBLEM_ID =
      SubtypePolymorphismChecker.SUBTYPE_MISSING_MEMFUNS_IMPL_PROBLEM_ID;

  public static final String MISSING_TEST_DOUBLE_SUBTYPE_PROBLEM_ID =
      MissingTestDoubleSubTypeChecker.MISSING_TEST_DOUBLE_SUBTYPE_PROBLEM_ID;

  public static final String MISSING_TEST_DOUBLE_STATICPOLY_PROBLEM_ID =
      MissingTestDoubleStaticPolyChecker.MISSING_TEST_DOUBLE_STATICPOLY_PROBLEM_ID;

  public static final String WRAPPED_FUNCTION_PROBLEM_ID =
      GnuOptionChecker.WRAP_FUNCTION_PROBLEM_ID;

  public static final String TRACE_FUNCTIONS_PROBLEM_ID =
      TraceFunctionChecker.TRACE_FUNCTIONS_PROBLEM_ID;

  public static final String INCONSISTENT_EXPECTATIONS_PROBLEM_ID =
      InconsistentExpectationsChecker.INCONSISTENT_EXPECTATIONS_PROBLEM_ID;
}
