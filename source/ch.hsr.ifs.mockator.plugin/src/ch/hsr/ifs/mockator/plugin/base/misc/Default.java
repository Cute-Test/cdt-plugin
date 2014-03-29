package ch.hsr.ifs.mockator.plugin.base.misc;

public abstract class Default {

  public static <T> T whenNull(T value, T defaultValue) {
    return value != null ? value : defaultValue;
  }
}
