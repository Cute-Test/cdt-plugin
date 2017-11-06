package ch.hsr.ifs.mockator.plugin.base.dbc;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.misc.CastHelper;


public abstract class Assert {

   public static void isTrue(final boolean expr, final String msg) {
      if (!expr) {
         throwWith(msg);
      }
   }

   public static void isFalse(final boolean expr, final String msg) {
      if (expr) {
         throwWith(msg);
      }
   }

   public static void notNull(final Object object, final String msg) {
      if (object == null) {
         throwWith(msg);
      }
   }

   public static <T> void instanceOf(final Object object, final Class<T> klass, final String msg) {
      if (!checkIsInstance(object, klass)) {
         throwWith(msg);
      }
   }

   public static <T> void notInstanceOf(final Object object, final Class<T> klass, final String msg) {
      if (checkIsInstance(object, klass)) {
         throwWith(msg);
      }
   }

   private static <T> boolean checkIsInstance(final Object object, final Class<T> klass) {
      return CastHelper.isInstanceOf(object, klass);
   }

   private static void throwWith(final String message) {
      throw new MockatorException(message);
   }
}
