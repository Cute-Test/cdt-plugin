package ch.hsr.ifs.mockator.plugin.base.misc;

import ch.hsr.ifs.iltis.cpp.ast.checker.helper.IProblemId;


public class IdHelper {

   public enum ProblemId implements IProblemId {
      //@formatter:off
      STATIC_POLY_MISSING_MEMFUNS_IMPL("ch.hsr.ifs.mockator.StaticPolyMissingMemFunsProblem"),
      INCONSISTENT_EXPECTATIONS("ch.hsr.ifs.mockator.InconsistentExpectationsProblem"),
      MISSING_TEST_DOUBLE_STATICPOLY("ch.hsr.ifs.mockator.MissingTestDoublStaticPolyProblem"),
      MISSING_TEST_DOUBLE_SUBTYPE("ch.hsr.ifs.mockator.MissingTestDoubleSubTypeProblem"),
      TRACE_FUNCTIONS("ch.hsr.ifs.mockator.TraceFunctionProblem"),
      WRAP_FUNCTION("ch.hsr.ifs.mockator.WrapFunctionProblem"),
      SUBTYPE_MISSING_MEMFUNS_IMPL("ch.hsr.ifs.mockator.SubtypeMissingMemFunsProblem");
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
