package ch.hsr.ifs.mockator.plugin.base.dbc;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.misc.CastHelper;

public abstract class Assert {

  public static void isTrue(boolean expr, String msg) {
    if (!expr) {
      throwWith(msg);
    }
  }

  public static void isFalse(boolean expr, String msg) {
    if (expr) {
      throwWith(msg);
    }
  }

  public static void notNull(Object object, String msg) {
    if (object == null) {
      throwWith(msg);
    }
  }

  public static <T> void instanceOf(Object object, Class<T> klass, String msg) {
    if (!checkIsInstance(object, klass)) {
      throwWith(msg);
    }
  }

  public static <T> void notInstanceOf(Object object, Class<T> klass, String msg) {
    if (checkIsInstance(object, klass)) {
      throwWith(msg);
    }
  }

  private static <T> boolean checkIsInstance(Object object, Class<T> klass) {
    return CastHelper.isInstanceOf(object, klass);
  }

  private static void throwWith(String message) {
    throw new MockatorException(message);
  }
}
