package ch.hsr.ifs.cute.mockator.base.misc;

import ch.hsr.ifs.iltis.cpp.core.ast.checker.helper.IProblemId;


public class IdHelper {

   public enum ProblemId implements IProblemId {
      //@formatter:off
      STATIC_POLY_MISSING_MEMFUNS_IMPL("ch.hsr.ifs.cute.mockator.StaticPolyMissingMemFunsProblem"),
      INCONSISTENT_EXPECTATIONS("ch.hsr.ifs.cute.mockator.InconsistentExpectationsProblem"),
      MISSING_TEST_DOUBLE_STATICPOLY("ch.hsr.ifs.cute.mockator.MissingTestDoublStaticPolyProblem"),
      MISSING_TEST_DOUBLE_SUBTYPE("ch.hsr.ifs.cute.mockator.MissingTestDoubleSubTypeProblem"),
      TRACE_FUNCTIONS("ch.hsr.ifs.cute.mockator.TraceFunctionProblem"),
      WRAP_FUNCTION("ch.hsr.ifs.cute.mockator.WrapFunctionProblem"),
      SUBTYPE_MISSING_MEMFUNS_IMPL("ch.hsr.ifs.cute.mockator.SubtypeMissingMemFunsProblem");
      //@formatter:on

      private final String id;

      private ProblemId(final String id) {
         this.id = id;
      }

      @Override
      public String getId() {
         return id;
      }

   }
}
