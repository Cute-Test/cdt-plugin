package ch.hsr.ifs.mockator.plugin.project.properties;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.head;
import static ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder.filter;
import static ch.hsr.ifs.mockator.plugin.base.misc.CastHelper.unsecureCast;

import java.util.Collection;

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;


class DefaultPropertyHandler {

   public static <E extends Enum<? extends PropertyTypeWithDefault>> E getDefault(final Class<E> enumKlass) {
      final Collection<E> defaults = filter(enumKlass.getEnumConstants(), (enumConst) -> {
         final PropertyTypeWithDefault typeWithDefault = unsecureCast(enumConst);
         return typeWithDefault.isDefault();
      });
      Assert.isTrue(defaults.size() == 1, "Exactly one default strategy expected");
      final E defaultType = unsecureCast(head(defaults).get());
      return defaultType;
   }
}
