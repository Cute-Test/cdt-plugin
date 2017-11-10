package ch.hsr.ifs.mockator.plugin.base.misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.hsr.ifs.iltis.core.exception.ILTISException;


public class DefaultCtorClassRegistry<T> {

   private final Collection<Class<? extends T>> classes;

   public DefaultCtorClassRegistry(final Collection<Class<? extends T>> classes) {
      this.classes = classes;
   }

   public Collection<T> createInstances() {
      final List<T> instances = new ArrayList<>();

      for (final Class<? extends T> klass : classes) {
         try {
            instances.add(klass.newInstance());
         }
         catch (final InstantiationException e) {
            new ILTISException("Class has no default constructor: " + klass.getSimpleName(), e).rethrowUnchecked();;
         }
         catch (final IllegalAccessException e) {
            new ILTISException("No access to class " + klass.getSimpleName(), e).rethrowUnchecked();;
         }
      }
      return instances;
   }
}
