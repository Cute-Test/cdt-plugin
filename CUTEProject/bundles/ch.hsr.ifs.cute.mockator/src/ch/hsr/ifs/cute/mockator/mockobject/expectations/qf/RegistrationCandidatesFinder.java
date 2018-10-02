package ch.hsr.ifs.cute.mockator.mockobject.expectations.qf;

import static ch.hsr.ifs.iltis.core.core.collections.CollectionUtil.list;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.collections.api.list.MutableList;

import ch.hsr.ifs.cute.mockator.mockobject.registrations.finder.CallRegistrationCollector;
import ch.hsr.ifs.cute.mockator.mockobject.registrations.finder.ExistingMemFunCallRegistration;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;


final class RegistrationCandidatesFinder {

   private final IASTTranslationUnit ast;
   private final CppStandard         cppStd;

   public RegistrationCandidatesFinder(final IASTTranslationUnit ast, final CppStandard cppStd) {
      this.ast = ast;
      this.cppStd = cppStd;
   }

   public MutableList<ExistingMemFunCallRegistration> findCallRegistrations(final IASTName callRegistrationVector) {
      final IASTName[] references = ast.getReferences(callRegistrationVector.resolveBinding());
      return getRegistrations(list(references));
   }

   private MutableList<ExistingMemFunCallRegistration> getRegistrations(final List<IASTName> callRegistrations) {
      return new CallRegistrationCollector(cppStd).getRegistrations(callRegistrations);
   }
}
