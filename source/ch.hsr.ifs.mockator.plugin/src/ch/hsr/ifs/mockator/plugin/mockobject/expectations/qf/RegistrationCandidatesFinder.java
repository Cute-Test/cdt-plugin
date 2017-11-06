package ch.hsr.ifs.mockator.plugin.mockobject.expectations.qf;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder.CallRegistrationCollector;
import ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder.ExistingMemFunCallRegistration;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;


class RegistrationCandidatesFinder {

   private final IASTTranslationUnit ast;
   private final CppStandard         cppStd;

   public RegistrationCandidatesFinder(final IASTTranslationUnit ast, final CppStandard cppStd) {
      this.ast = ast;
      this.cppStd = cppStd;
   }

   public Collection<ExistingMemFunCallRegistration> findCallRegistrations(final IASTName callRegistrationVector) {
      final IASTName[] references = ast.getReferences(callRegistrationVector.resolveBinding());
      return getRegistrations(list(references));
   }

   private Collection<ExistingMemFunCallRegistration> getRegistrations(final List<IASTName> callRegistrations) {
      return new CallRegistrationCollector(cppStd).getRegistrations(callRegistrations);
   }
}
