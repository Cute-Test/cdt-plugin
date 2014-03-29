package ch.hsr.ifs.mockator.plugin.refsupport.utils;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import org.eclipse.cdt.core.dom.ast.IBinding;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;

public class BindingTypeVerifier {

  public static <T> boolean isOfType(IBinding binding, Class<T> klass) {
    return getAdapter(binding, klass).isSome();
  }

  public static <T> Maybe<T> getAsType(IBinding binding, Class<T> klass) {
    return getAdapter(binding, klass);
  }

  @SuppressWarnings("unchecked")
  private static <T> Maybe<T> getAdapter(IBinding binding, Class<T> klass) {
    if (binding == null)
      return none();

    return maybe((T) binding.getAdapter(klass));
  }
}
