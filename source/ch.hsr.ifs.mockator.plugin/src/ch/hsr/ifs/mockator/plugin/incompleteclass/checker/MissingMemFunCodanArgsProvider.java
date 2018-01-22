package ch.hsr.ifs.mockator.plugin.incompleteclass.checker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;

import ch.hsr.ifs.mockator.plugin.fakeobject.FakeObjectDefaultCtorProvider;
import ch.hsr.ifs.mockator.plugin.incompleteclass.DefaultCtorProvider;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.mockator.plugin.mockobject.MockObjectDefaultCtorProvider;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;


class MissingMemFunCodanArgsProvider {

   private final CppStandard                                 cppStd;
   private final Collection<? extends MissingMemberFunction> missingMemFuns;
   private final ICPPASTCompositeTypeSpecifier               clazz;

   public MissingMemFunCodanArgsProvider(final CppStandard cppStd, final Collection<? extends MissingMemberFunction> missingMemFuns,
                                         final ICPPASTCompositeTypeSpecifier clazz) {
      this.cppStd = cppStd;
      this.missingMemFuns = missingMemFuns;
      this.clazz = clazz;
   }

   public Optional<MissingMemFunCodanArguments> createMemFunCodanArgs() {
      if (missingMemFuns.isEmpty()) {
         return Optional.empty();
      }

      final Collection<MissingMemberFunction> fake = collectMissingMemFuns(getFakeCtorProvider());
      final Collection<MissingMemberFunction> mock = collectMissingMemFuns(getMockCtorProvider());
      final String className = clazz.getName().toString();
      return Optional.of(new MissingMemFunCodanArguments(className, getFunSignatures(fake), getFunSignatures(mock)));
   }

   private MockObjectDefaultCtorProvider getMockCtorProvider() {
      return new MockObjectDefaultCtorProvider(clazz, cppStd);
   }

   private FakeObjectDefaultCtorProvider getFakeCtorProvider() {
      return new FakeObjectDefaultCtorProvider(clazz);
   }

   private List<MissingMemberFunction> collectMissingMemFuns(final DefaultCtorProvider defaultCtorProvider) {
      final List<MissingMemberFunction> memFuns = new ArrayList<>();
      memFuns.addAll(missingMemFuns);

      defaultCtorProvider.createMissingDefaultCtor(missingMemFuns).ifPresent((defaultCtor) -> memFuns.add(0, defaultCtor));
      return memFuns;
   }

   private static String getFunSignatures(final Collection<MissingMemberFunction> missingMemFuns) {
      return new MissingMemFunSignaturesGenerator(missingMemFuns).getSignaturesWithStatistics();
   }
}
