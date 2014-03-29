package ch.hsr.ifs.mockator.plugin.mockobject;

import static ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder.filter;
import static ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder.map;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.functional.F1;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.incompleteclass.DefaultCtorProvider;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.DefaultConstructor;

public class MockObjectDefaultCtorProvider implements DefaultCtorProvider {
  private final MockObject mockObject;
  private final CppStandard cppStd;

  public MockObjectDefaultCtorProvider(ICPPASTCompositeTypeSpecifier klass, CppStandard cppStd) {
    this.mockObject = new MockObject(klass);
    this.cppStd = cppStd;
  }

  @Override
  public Maybe<DefaultConstructor> createMissingDefaultCtor(
      Collection<? extends MissingMemberFunction> memFuns) {
    switch (mockObject.getPolymorphismKind()) {
      case StaticPoly:
        return handleStaticPoly(memFuns);
      case SubTypePoly:
        return handleSubTypePoly();
      default:
        throw new MockatorException("Not supported polymorphism kind");
    }
  }

  private Maybe<DefaultConstructor> handleSubTypePoly() {
    if (!mockObject.hasPublicCtor())
      return maybe(new DefaultConstructor(mockObject));

    return none();
  }

  private Maybe<DefaultConstructor> handleStaticPoly(
      Collection<? extends MissingMemberFunction> memFuns) {
    if (!hasPublicCtors(memFuns) && !mockObject.hasOnlyStaticFunctions(memFuns))
      return maybe(new DefaultConstructor(mockObject));

    return none();
  }

  private boolean hasPublicCtors(Collection<? extends MissingMemberFunction> memFuns) {
    Collection<ICPPASTFunctionDefinition> onlyCtors =
        filter(toFunctions(memFuns), new F1<ICPPASTFunctionDefinition, Boolean>() {
          @Override
          public Boolean apply(ICPPASTFunctionDefinition function) {
            return AstUtil.isDeclConstructor(function);
          }
        });
    return mockObject.hasPublicCtor() || !onlyCtors.isEmpty();
  }

  private <T extends MissingMemberFunction> Collection<ICPPASTFunctionDefinition> toFunctions(
      Collection<T> memFuns) {
    return map(memFuns, new F1<T, ICPPASTFunctionDefinition>() {
      @Override
      public ICPPASTFunctionDefinition apply(T missingMemFun) {
        MockObjectMemFunImplStrategy strategy =
            new MockObjectMemFunImplStrategy(cppStd, mockObject);
        return missingMemFun.createFunctionDefinition(strategy, cppStd);
      }
    });
  }
}
