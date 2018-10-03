package ch.hsr.ifs.cute.mockator.incompleteclass.checker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;

import ch.hsr.ifs.cute.mockator.fakeobject.FakeObjectDefaultCtorProvider;
import ch.hsr.ifs.cute.mockator.incompleteclass.DefaultCtorProvider;
import ch.hsr.ifs.cute.mockator.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.cute.mockator.infos.MissingMemFunInfo;
import ch.hsr.ifs.cute.mockator.mockobject.MockObjectDefaultCtorProvider;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;


class MissingMemFunInfoProvider {

    private final CppStandard                                 cppStd;
    private final Collection<? extends MissingMemberFunction> missingMemFuns;
    private final ICPPASTCompositeTypeSpecifier               clazz;

    public MissingMemFunInfoProvider(final CppStandard cppStd, final Collection<? extends MissingMemberFunction> missingMemFuns,
                                     final ICPPASTCompositeTypeSpecifier clazz) {
        this.cppStd = cppStd;
        this.missingMemFuns = missingMemFuns;
        this.clazz = clazz;
    }

    public Optional<MissingMemFunInfo> createInfo() {
        if (missingMemFuns.isEmpty()) return Optional.empty();

        return Optional.of(new MissingMemFunInfo().also(i -> {
            i.testDoubleName = clazz.getName().toString();
            i.missingMemFunsForFake = getFunSignatures(collectMissingMemFuns(getFakeCtorProvider()));
            i.missingMemFunsForMock = getFunSignatures(collectMissingMemFuns(getMockCtorProvider()));
        }));
    }

    public Optional<MissingMemFunInfo> createMemFunCodanArgs() {
        if (missingMemFuns.isEmpty()) return Optional.empty();
        return Optional.of(new MissingMemFunInfo().also(i -> {
            i.testDoubleName = clazz.getName().toString();
            i.missingMemFunsForFake = getFunSignatures(collectMissingMemFuns(getFakeCtorProvider()));
            i.missingMemFunsForMock = getFunSignatures(collectMissingMemFuns(getMockCtorProvider()));
        }));

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
