//package ch.hsr.ifs.cute.mockator.REMOVE_AFTER_TESTING;
//
//import static ch.hsr.ifs.iltis.core.core.collections.CollectionUtil.array;
//
//import org.eclipse.core.resources.IMarker;
//
//import ch.hsr.ifs.cute.mockator.testdouble.creation.subtype.ArgumentPassByStrategy;
//
//
//public class CreateTestDoubleSubTypeCodanArgs extends CodanArguments {
//
//   private final String nameOfMissingInstance;
//   private final String parentClassName;
//   private final String targetIncludePath;
//   private final String passByStrategy;
//
//   public CreateTestDoubleSubTypeCodanArgs(final IMarker marker) {
//      final String[] problemArgs = getProblemArguments(marker);
//      nameOfMissingInstance = problemArgs[0];
//      parentClassName = problemArgs[1];
//      targetIncludePath = problemArgs[2];
//      passByStrategy = problemArgs[3];
//   }
//
//   public CreateTestDoubleSubTypeCodanArgs(final String nameOfMissingInstance, final String parentClassName, final String targetIncludePath,
//                                           final String passByStrategy) {
//      this.nameOfMissingInstance = nameOfMissingInstance;
//      this.parentClassName = parentClassName;
//      this.targetIncludePath = targetIncludePath;
//      this.passByStrategy = passByStrategy;
//   }
//
//   @Override
//   public Object[] toArray() {
//      return array(nameOfMissingInstance, parentClassName, targetIncludePath, passByStrategy);
//   }
//
//   public String getNameOfMissingInstance() {
//      return nameOfMissingInstance;
//   }
//
//   public String getParentClassName() {
//      return parentClassName;
//   }
//
//   public String getTargetIncludePath() {
//      return targetIncludePath;
//   }
//
//   public ArgumentPassByStrategy getPassByStrategy() {
//      return ArgumentPassByStrategy.valueOf(passByStrategy);
//   }
//
//   @Override
//   public int getNumOfProblemArguments() {
//      return 4;
//   }
//}
