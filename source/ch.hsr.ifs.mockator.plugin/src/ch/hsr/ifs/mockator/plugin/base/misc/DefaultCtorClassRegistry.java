package ch.hsr.ifs.mockator.plugin.base.misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;

public class DefaultCtorClassRegistry<T> {
  private final Collection<Class<? extends T>> classes;

  public DefaultCtorClassRegistry(Collection<Class<? extends T>> classes) {
    this.classes = classes;
  }

  public Collection<T> createInstances() {
    List<T> instances = new ArrayList<T>();

    for (Class<? extends T> klass : classes) {
      try {
        instances.add(klass.newInstance());
      } catch (InstantiationException e) {
        throw new MockatorException("Class has no default constructor: " + klass.getSimpleName(), e);
      } catch (IllegalAccessException e) {
        throw new MockatorException("No access to class " + klass.getSimpleName(), e);
      }
    }
    return instances;
  }
}
