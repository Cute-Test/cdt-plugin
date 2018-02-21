package ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTName;

import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStdFactory;


public class CallRegistrationCollector {

   private final CppStandard cppStd;

   public CallRegistrationCollector(final CppStandard cppStd) {
      this.cppStd = cppStd;
   }

   public Collection<ExistingMemFunCallRegistration> getRegistrations(final List<IASTName> registrationVectorUses) {
      final List<ExistingMemFunCallRegistration> callRegistrations = new ArrayList<>();
      final RegistrationFinder finder = getRegistrationFinder();

      for (final IASTName usage : registrationVectorUses) {
         finder.findRegistration(usage).ifPresent((candidate) -> callRegistrations.add(candidate));
      }
      return callRegistrations;
   }

   private RegistrationFinder getRegistrationFinder() {
      return CppStdFactory.from(Cpp03RegistrationFinder.class, Cpp11RegistrationFinder.class).getHandler(cppStd);
   }
}
