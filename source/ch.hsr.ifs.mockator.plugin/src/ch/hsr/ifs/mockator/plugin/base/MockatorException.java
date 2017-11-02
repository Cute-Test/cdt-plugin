package ch.hsr.ifs.mockator.plugin.base;

public class MockatorException extends RuntimeException {

   private static final long serialVersionUID = 1L;
   private Exception         originalException;

   public MockatorException(final String message) {
      super(message);
   }

   public MockatorException(final Exception originalException) {
      super(originalException);
      this.originalException = originalException;
   }

   public MockatorException(final String message, final Throwable cause) {
      super(message, cause);
   }

   /*
    * Use case: Helps in a catching clause at a higher level in the code
    * and to look for particular types of exceptions:
    * catch(MockatorException e) {
    * try {
    * e.rethrow();
    * } catch(IllegalArgumentException e) {
    * // ...
    * } catch(FileNotFoundException e) {
    * // ...
    * }
    */
   public void rethrow() throws Exception {
      if (originalException != null) {
         throw originalException;
      }
   }
}
