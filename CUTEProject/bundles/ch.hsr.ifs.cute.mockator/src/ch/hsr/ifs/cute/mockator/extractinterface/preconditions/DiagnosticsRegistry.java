package ch.hsr.ifs.cute.mockator.extractinterface.preconditions;

import static ch.hsr.ifs.iltis.core.collections.CollectionUtil.orderPreservingSet;

import java.util.Set;
import java.util.function.Consumer;

import ch.hsr.ifs.cute.mockator.base.misc.DefaultCtorClassRegistry;
import ch.hsr.ifs.cute.mockator.extractinterface.context.ExtractInterfaceContext;


public class DiagnosticsRegistry extends DefaultCtorClassRegistry<Consumer<ExtractInterfaceContext>> {

    private static final Set<Class<? extends Consumer<ExtractInterfaceContext>>> DIAGNOSTICS = orderPreservingSet(
   //@formatter:off
         ClassDefinitionLookup.class,
         MemFunCollector.class,
         IncludeDirectiveCollector.class,
         ForwardDeclCollector.class,
         TypeDefCollector.class,
         NewInterfaceNameProposal.class
         //@formatter:on
    );

    public DiagnosticsRegistry() {
        super(DIAGNOSTICS);
    }
}
