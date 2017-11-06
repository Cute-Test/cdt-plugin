package ch.hsr.ifs.mockator.plugin.base.misc;

public abstract class Default {

   public static <T> T whenNull(final T value, final T defaultValue) {
      return value != null ? value : defaultValue;
   }
}
