package ch.hsr.ifs.cute.mockator.ids;

import org.osgi.framework.FrameworkUtil;

import ch.hsr.ifs.iltis.cpp.core.ast.checker.helper.IProblemId;
import ch.hsr.ifs.iltis.cpp.core.ids.IRefactoringId;


public class IdHelper {

   public static final String PLUGIN_ID         = FrameworkUtil.getBundle(IdHelper.class).getSymbolicName();
   public static final String DEFAULT_QUALIFIER = PLUGIN_ID;

   public static final String PREFERENCES_PREFIX  = DEFAULT_QUALIFIER + ".preferences.";
   public static final String PROBLEMS_PREFIX     = DEFAULT_QUALIFIER + ".problems.";
   public static final String REFACTORINGS_PREFIX = DEFAULT_QUALIFIER + ".refactorings.";

   public enum ProblemId implements IProblemId<ProblemId> {
      STATIC_POLY_MISSING_MEMFUNS_IMPL(PROBLEMS_PREFIX + "StaticPolyMissingMemFunsProblem"), //
      INCONSISTENT_EXPECTATIONS(PROBLEMS_PREFIX + "InconsistentExpectationsProblem"), //
      MISSING_TEST_DOUBLE_STATICPOLY(PROBLEMS_PREFIX + "MissingTestDoubleStaticPolyProblem"), //
      MISSING_TEST_DOUBLE_SUBTYPE(PROBLEMS_PREFIX + "MissingTestDoubleSubTypeProblem"), //
      TRACE_FUNCTIONS(PROBLEMS_PREFIX + "TraceFunctionProblem"), //
      WRAP_FUNCTION(PROBLEMS_PREFIX + "WrapFunctionProblem"), //
      SUBTYPE_MISSING_MEMFUNS_IMPL(PROBLEMS_PREFIX + "SubtypeMissingMemFunsProblem");

      public final String id;

      ProblemId(final String id) {
         this.id = id;
      }

      @Override
      public String getId() {
         return id;
      }

      @Override
      public String toString() {
         return id;
      }

      public static ProblemId of(String string) {
         for (final ProblemId id : values()) {
            if (id.getId().equals(string)) return id;
         }
         throw new IllegalArgumentException("Illegal ProblemId: " + string);
      }

      @Override
      public ProblemId unstringify(String string) {
         return of(string);
      }

      @Override
      public String stringify() {
         return id;
      }

   }

   public enum RefactoringId implements IRefactoringId<RefactoringId> {
      EXTRACT_INTERFACE(REFACTORINGS_PREFIX + "ExtractInterface");//

      public final String id;

      RefactoringId(final String id) {
         this.id = id;
      }

      @Override
      public String getId() {
         return id;
      }

      @Override
      public String toString() {
         return id;
      }

      public static RefactoringId of(String string) {
         for (final RefactoringId id : values()) {
            if (id.getId().equals(string)) return id;
         }
         throw new IllegalArgumentException("Illegal RefactoringId: " + string);
      }

      @Override
      public RefactoringId unstringify(String string) {
         return of(string);
      }

      @Override
      public String stringify() {
         return id;
      }

   }

}
