package ch.hsr.ifs.mockator.plugin.extractinterface.transform;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;

import java.util.Set;
import java.util.function.Consumer;

import ch.hsr.ifs.mockator.plugin.base.misc.DefaultCtorClassRegistry;
import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;


public class TransformationsRegistry extends DefaultCtorClassRegistry<Consumer<ExtractInterfaceContext>> {

   @SuppressWarnings("unchecked")
   private static final Set<Class<? extends Consumer<ExtractInterfaceContext>>> TRANSFORMATIONS = orderPreservingSet(
         //TODO formatting
         //@formatter:off
      ExistingReferencesReplacer.class, PublicInheritanceAdder.class, InterfaceFileCreator.class, InterfaceIncludeInserter.class,
      InterfaceClassCreator.class, ForwardDeclsRemover.class, TypeDefsRemover.class
  //@formatter:on
   );

   public TransformationsRegistry() {
      super(TRANSFORMATIONS);
   }
}
