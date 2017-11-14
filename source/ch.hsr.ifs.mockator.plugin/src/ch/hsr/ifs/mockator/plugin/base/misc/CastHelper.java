package ch.hsr.ifs.mockator.plugin.base.misc;

public abstract class CastHelper {

   public static <T> boolean isInstanceOf(final Object obj, final Class<T> klass) {
      return klass.isAssignableFrom(obj.getClass());
   }
}
