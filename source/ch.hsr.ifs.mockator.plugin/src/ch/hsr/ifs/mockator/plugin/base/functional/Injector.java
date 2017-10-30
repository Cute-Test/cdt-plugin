package ch.hsr.ifs.mockator.plugin.base.functional;

public interface Injector<S, T> extends F1V<S> {

   T yield();
}
