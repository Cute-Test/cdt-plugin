package ch.hsr.ifs.mockator.plugin.incompleteclass.checker;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.fakeobject.FakeObjectDefaultCtorProvider;
import ch.hsr.ifs.mockator.plugin.incompleteclass.DefaultCtorProvider;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.mockator.plugin.mockobject.MockObjectDefaultCtorProvider;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;

class MissingMemFunCodanArgsProvider {
  private final CppStandard cppStd;
  private final Collection<? extends MissingMemberFunction> missingMemFuns;
  private final ICPPASTCompositeTypeSpecifier klass;

  public MissingMemFunCodanArgsProvider(CppStandard cppStd,
      Collection<? extends MissingMemberFunction> missingMemFuns,
      ICPPASTCompositeTypeSpecifier klass) {
    this.cppStd = cppStd;
    this.missingMemFuns = missingMemFuns;
    this.klass = klass;
  }

  public Maybe<MissingMemFunCodanArguments> createMemFunCodanArgs() {
    if (missingMemFuns.isEmpty())
      return none();

    Collection<MissingMemberFunction> fake = collectMissingMemFuns(getFakeCtorProvider());
    Collection<MissingMemberFunction> mock = collectMissingMemFuns(getMockCtorProvider());
    String className = klass.getName().toString();
    return maybe(new MissingMemFunCodanArguments(className, getFunSignatures(fake),
        getFunSignatures(mock)));
  }

  private MockObjectDefaultCtorProvider getMockCtorProvider() {
    return new MockObjectDefaultCtorProvider(klass, cppStd);
  }

  private FakeObjectDefaultCtorProvider getFakeCtorProvider() {
    return new FakeObjectDefaultCtorProvider(klass);
  }

  private List<MissingMemberFunction> collectMissingMemFuns(DefaultCtorProvider defaultCtorProvider) {
    List<MissingMemberFunction> memFuns = list();
    memFuns.addAll(missingMemFuns);

    for (MissingMemberFunction optDefaultCtor : defaultCtorProvider
        .createMissingDefaultCtor(missingMemFuns)) {
      memFuns.add(0, optDefaultCtor);
    }

    return memFuns;
  }

  private static String getFunSignatures(Collection<MissingMemberFunction> missingMemFuns) {
    return new MissingMemFunSignaturesGenerator(missingMemFuns).getSignaturesWithStatistics();
  }
}
