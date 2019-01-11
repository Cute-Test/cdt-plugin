package ch.hsr.ifs.cute.mockator.mockobject.registrations.finder;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.project.properties.CppStdFactory;


public class CallRegistrationCollector {

    private final CppStandard cppStd;

    public CallRegistrationCollector(final CppStandard cppStd) {
        this.cppStd = cppStd;
    }

    public MutableList<ExistingMemFunCallRegistration> getRegistrations(final List<IASTName> registrationVectorUses) {
        final MutableList<ExistingMemFunCallRegistration> callRegistrations = Lists.mutable.empty();
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
