package ch.hsr.ifs.mockator.plugin.mockobject;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;

import ch.hsr.ifs.mockator.plugin.incompleteclass.DefaultCtorProvider;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.DefaultConstructor;


public class MockObjectDefaultCtorProvider implements DefaultCtorProvider {

   private final MockObject  mockObject;
   private final CppStandard cppStd;

   public MockObjectDefaultCtorProvider(final ICPPASTCompositeTypeSpecifier clazz, final CppStandard cppStd) {
      mockObject = new MockObject(clazz);
      this.cppStd = cppStd;
   }

   @Override
   public Optional<DefaultConstructor> createMissingDefaultCtor(final Collection<? extends MissingMemberFunction> memFuns) {
      switch (mockObject.getPolymorphismKind()) {
      case StaticPoly:
         return handleStaticPoly(memFuns);
      case SubTypePoly:
         return handleSubTypePoly();
      default:
         throw new ILTISException("Not supported polymorphism kind").rethrowUnchecked();
      }
   }

   private Optional<DefaultConstructor> handleSubTypePoly() {
      if (!mockObject.hasPublicCtor()) {
         return Optional.of(new DefaultConstructor(mockObject));
      }

      return Optional.empty();
   }

   private Optional<DefaultConstructor> handleStaticPoly(final Collection<? extends MissingMemberFunction> memFuns) {
      if (!hasPublicCtors(memFuns) && !mockObject.hasOnlyStaticFunctions(memFuns)) {
         return Optional.of(new DefaultConstructor(mockObject));
      }

      return Optional.empty();
   }

   private boolean hasPublicCtors(final Collection<? extends MissingMemberFunction> memFuns) {
      final Collection<ICPPASTFunctionDefinition> onlyCtors = toFunctions(memFuns).stream().filter((function) -> ASTUtil.isDeclConstructor(function))
               .collect(Collectors.toList());
      return mockObject.hasPublicCtor() || !onlyCtors.isEmpty();
   }

   private <T extends MissingMemberFunction> Collection<ICPPASTFunctionDefinition> toFunctions(final Collection<T> memFuns) {
      return memFuns.stream().map((missingMemFun) -> {
         final MockObjectMemFunImplStrategy strategy = new MockObjectMemFunImplStrategy(cppStd, mockObject);
         return missingMemFun.createFunctionDefinition(strategy, cppStd);
      }).collect(Collectors.toList());
   }
}
