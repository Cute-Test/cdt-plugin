package ch.hsr.ifs.mockator.plugin.project.properties;

import static ch.hsr.ifs.iltis.core.collections.CollectionUtil.head;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.iltis.core.functional.Functional;


class DefaultPropertyHandler {

   public static <E extends Enum<? extends PropertyTypeWithDefault>> E getDefault(final Class<E> enumKlass) {
      final Collection<E> defaults = Arrays.asList(enumKlass.getEnumConstants()).stream().filter((enumConst) -> {
         final PropertyTypeWithDefault typeWithDefault = Functional.as(enumConst);
         return typeWithDefault.isDefault();
      }).collect(Collectors.toList());
      ILTISException.Unless.isTrue(defaults.size() == 1, "Exactly one default strategy expected");
      final E defaultType = Functional.as(head(defaults).get());
      return defaultType;
   }
}
