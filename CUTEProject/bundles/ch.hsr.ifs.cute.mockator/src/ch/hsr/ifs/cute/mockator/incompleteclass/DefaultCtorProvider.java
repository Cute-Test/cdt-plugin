package ch.hsr.ifs.cute.mockator.incompleteclass;

import java.util.Collection;
import java.util.Optional;


public interface DefaultCtorProvider {

   Optional<? extends MissingMemberFunction> createMissingDefaultCtor(Collection<? extends MissingMemberFunction> missingMemFuns);
}
