package ch.hsr.ifs.cute.mockator.base.misc;

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

        for (final Class<? extends T> clazz : classes) {
            try {
                instances.add(clazz.newInstance());
            } catch (final InstantiationException e) {
                new ILTISException("Class has no default constructor: " + clazz.getSimpleName(), e).rethrowUnchecked();;
            } catch (final IllegalAccessException e) {
                new ILTISException("No access to class " + clazz.getSimpleName(), e).rethrowUnchecked();;
            }
        }
        return instances;
    }
}
