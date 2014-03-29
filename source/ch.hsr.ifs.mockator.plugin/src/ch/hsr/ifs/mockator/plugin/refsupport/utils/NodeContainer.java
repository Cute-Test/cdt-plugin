package ch.hsr.ifs.mockator.plugin.refsupport.utils;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;

public class NodeContainer<T> {
  private T node;

  public NodeContainer(T node) {
    this.node = node;
  }

  public NodeContainer() {}

  public Maybe<T> getNode() {
    return maybe(node);
  }

  public void setNode(T node) {
    this.node = node;
  }
}
