package ch.hsr.ifs.mockator.plugin.fakeobject;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.incompleteclass.DefaultCtorProvider;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.testdouble.PolymorphismKind;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.DefaultConstructor;
import ch.hsr.ifs.mockator.plugin.testdouble.support.BaseClassCtorCallHandler;

public class FakeObjectDefaultCtorProvider implements DefaultCtorProvider {
  private final FakeObject fakeObject;

  public FakeObjectDefaultCtorProvider(ICPPASTCompositeTypeSpecifier klass) {
    fakeObject = new FakeObject(klass);
  }

  @Override
  public Maybe<? extends MissingMemberFunction> createMissingDefaultCtor(
      Collection<? extends MissingMemberFunction> memFuns) {
    if (fakeObject.getPolymorphismKind() != PolymorphismKind.SubTypePoly)
      return none();

    if (!hasBaseClassDefaultCtor() && hasOnlyImplicitDefaultCtor())
      return maybe(new DefaultConstructor(fakeObject));

    return none();
  }

  private boolean hasBaseClassDefaultCtor() {
    BaseClassCtorCallHandler handler = new BaseClassCtorCallHandler(fakeObject.getClassType());
    return handler.hasBaseClassDefaultCtor();
  }

  private boolean hasOnlyImplicitDefaultCtor() {
    ICPPConstructor[] ctors = fakeObject.getClassType().getConstructors();
    // 2 = 1 implicit default + 1 copy ctor
    return ctors.length <= 2 && ctors[0].isImplicit() && AstUtil.isDefaultCtor(ctors[0]);
  }
}
