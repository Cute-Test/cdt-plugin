package ch.hsr.ifs.mockator.plugin.base.misc;

public abstract class CastHelper {

  public static <T> boolean isInstanceOf(Object obj, Class<T> klass) {
    return klass.isAssignableFrom(obj.getClass());
  }

  // simple helper to avoid having unchecked warnings in our code base
  @SuppressWarnings("unchecked")
  public static <T> T unsecureCast(Object o) {
    return (T) o;
  }
}
