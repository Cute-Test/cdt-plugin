package ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTName;

import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStdFactory;

public class CallRegistrationCollector {
  private final CppStandard cppStd;

  public CallRegistrationCollector(CppStandard cppStd) {
    this.cppStd = cppStd;
  }

  public Collection<ExistingMemFunCallRegistration> getRegistrations(
      List<IASTName> registrationVectorUses) {
    List<ExistingMemFunCallRegistration> callRegistrations = list();
    RegistrationFinder finder = getRegistrationFinder();

    for (IASTName usage : registrationVectorUses) {
      for (ExistingMemFunCallRegistration candidate : finder.findRegistration(usage)) {
        callRegistrations.add(candidate);
      }
    }
    return callRegistrations;
  }

  private RegistrationFinder getRegistrationFinder() {
    return CppStdFactory.from(Cpp03RegistrationFinder.class, Cpp11RegistrationFinder.class)
        .getHandler(cppStd);
  }
}
