package ch.hsr.ifs.mockator.plugin.project.properties;

import static ch.hsr.ifs.iltis.core.functional.FunHelper.as;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.head;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;


class DefaultPropertyHandler {

   public static <E extends Enum<? extends PropertyTypeWithDefault>> E getDefault(final Class<E> enumKlass) {
      final Collection<E> defaults = Arrays.asList(enumKlass.getEnumConstants()).stream().filter((enumConst) -> {
         final PropertyTypeWithDefault typeWithDefault = as(enumConst);
         return typeWithDefault.isDefault();
      }).collect(Collectors.toList());
      Assert.isTrue(defaults.size() == 1, "Exactly one default strategy expected");
      final E defaultType = as(head(defaults).get());
      return defaultType;
   }
}
