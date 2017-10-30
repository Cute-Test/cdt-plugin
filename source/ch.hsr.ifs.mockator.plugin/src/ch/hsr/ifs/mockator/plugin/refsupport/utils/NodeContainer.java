package ch.hsr.ifs.mockator.plugin.refsupport.utils;

import java.util.Optional;


public class NodeContainer<T> {

   private T node;

   public NodeContainer(final T node) {
      this.node = node;
   }

   public NodeContainer() {}

   public Optional<T> getNode() {
      return Optional.ofNullable(node);
   }

   public void setNode(final T node) {
      this.node = node;
   }
}
