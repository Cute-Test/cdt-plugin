package ch.hsr.ifs.mockator.plugin.base.functional;

import java.util.function.Consumer;


public interface Injector<S, T> extends Consumer<S> {

   T yield();
}
