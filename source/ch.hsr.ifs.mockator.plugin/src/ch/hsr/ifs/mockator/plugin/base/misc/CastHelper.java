package ch.hsr.ifs.mockator.plugin.base.misc;

public abstract class CastHelper {

   //TODO does not describe well what it does

   public static <T> boolean isInstanceOf(final Object obj, final Class<T> clazz) {
      return clazz.isAssignableFrom(obj.getClass());
   }
}
