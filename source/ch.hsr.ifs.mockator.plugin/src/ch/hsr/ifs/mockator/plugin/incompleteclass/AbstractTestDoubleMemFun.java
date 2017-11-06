package ch.hsr.ifs.mockator.plugin.incompleteclass;

public abstract class AbstractTestDoubleMemFun implements TestDoubleMemFun, Comparable<TestDoubleMemFun> {

   @Override
   public String toString() {
      return getFunctionSignature();
   }

   @Override
   public int hashCode() {
      return getFunctionSignature().hashCode();
   }

   @Override
   public boolean equals(final Object obj) {
      if (obj == this) return true;

      if (!(obj instanceof TestDoubleMemFun)) return false;

      final TestDoubleMemFun rhs = (TestDoubleMemFun) obj;
      return rhs.getFunctionSignature().equals(getFunctionSignature());
   }

   @Override
   public int compareTo(final TestDoubleMemFun other) {
      return getFunctionSignature().compareTo(other.getFunctionSignature());
   }
}
