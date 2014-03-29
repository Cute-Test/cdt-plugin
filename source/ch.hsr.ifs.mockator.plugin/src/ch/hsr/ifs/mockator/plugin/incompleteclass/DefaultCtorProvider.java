package ch.hsr.ifs.mockator.plugin.incompleteclass;

import java.util.Collection;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;

public interface DefaultCtorProvider {
  Maybe<? extends MissingMemberFunction> createMissingDefaultCtor(
      Collection<? extends MissingMemberFunction> missingMemFuns);
}
