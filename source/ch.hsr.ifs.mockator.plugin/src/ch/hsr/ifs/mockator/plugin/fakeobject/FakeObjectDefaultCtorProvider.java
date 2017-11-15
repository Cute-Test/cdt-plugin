package ch.hsr.ifs.mockator.plugin.fakeobject;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;

import ch.hsr.ifs.mockator.plugin.incompleteclass.DefaultCtorProvider;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;
import ch.hsr.ifs.mockator.plugin.testdouble.PolymorphismKind;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.DefaultConstructor;
import ch.hsr.ifs.mockator.plugin.testdouble.support.BaseClassCtorCallHandler;


public class FakeObjectDefaultCtorProvider implements DefaultCtorProvider {

   private final FakeObject fakeObject;

   public FakeObjectDefaultCtorProvider(final ICPPASTCompositeTypeSpecifier clazz) {
      fakeObject = new FakeObject(clazz);
   }

   @Override
   public Optional<? extends MissingMemberFunction> createMissingDefaultCtor(final Collection<? extends MissingMemberFunction> memFuns) {
      if (fakeObject.getPolymorphismKind() != PolymorphismKind.SubTypePoly) { return Optional.empty(); }

      if (!hasBaseClassDefaultCtor() && hasOnlyImplicitDefaultCtor()) { return Optional.of(new DefaultConstructor(fakeObject)); }

      return Optional.empty();
   }

   private boolean hasBaseClassDefaultCtor() {
      final BaseClassCtorCallHandler handler = new BaseClassCtorCallHandler(fakeObject.getClassType());
      return handler.hasBaseClassDefaultCtor();
   }

   private boolean hasOnlyImplicitDefaultCtor() {
      final ICPPConstructor[] ctors = fakeObject.getClassType().getConstructors();
      // 2 = 1 implicit default + 1 copy ctor
      return ctors.length <= 2 && ctors[0].isImplicit() && ASTUtil.isDefaultCtor(ctors[0]);
   }
}
