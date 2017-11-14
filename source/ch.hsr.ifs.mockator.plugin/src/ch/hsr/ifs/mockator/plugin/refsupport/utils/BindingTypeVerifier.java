package ch.hsr.ifs.mockator.plugin.refsupport.utils;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IBinding;


public class BindingTypeVerifier {

   public static <T> boolean isOfType(final IBinding binding, final Class<T> klass) {
      return getAdapter(binding, klass).isPresent();
   }

   public static <T> Optional<T> getAsType(final IBinding binding, final Class<T> klass) {
      return getAdapter(binding, klass);
   }

   private static <T> Optional<T> getAdapter(final IBinding binding, final Class<T> klass) {
      if (binding == null) { return Optional.empty(); }

      return Optional.ofNullable(binding.getAdapter(klass));
   }
}
