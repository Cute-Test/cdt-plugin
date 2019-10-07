package ch.hsr.ifs.cute.headers.versions;

import ch.hsr.ifs.iltis.cpp.versionator.definition.CPPVersion;

import ch.hsr.ifs.cute.core.headers.CuteVersionNumber;
import ch.hsr.ifs.cute.headers.ICuteHeaders;


/**
 *
 * @author tstauber
 *
 */
public enum CuteHeaders2 implements ICuteHeaders {

    @CuteVersionNumber(major = 2, minor = 2, patch = 1)
    _2_1(),
    @CuteVersionNumber(major = 2, minor = 2, patch = 3)
    _2_3(),
    @CuteVersionNumber(major = 2, minor = 2, patch = 5)
    _2_5();

    private CuteVersionNumber fVersionNumber;

    CuteHeaders2() {
        try {
            fVersionNumber = CuteHeaders2.class.getField(name()).getAnnotation(CuteVersionNumber.class);
        } catch (NoSuchFieldException | SecurityException e) {
            throwAnnotationMissingException(name(), CuteHeaders2.class.getSimpleName());
        }
        if (fVersionNumber == null) throwAnnotationMissingException(name(), CuteHeaders2.class.getSimpleName());
    }

    @Override
    public CuteVersionNumber getVersionNumber() {
        return fVersionNumber;
    }

    @Override
    public boolean compatibleWith(CPPVersion cppVersion) {
        return cppVersion.ordinal() >= CPPVersion.CPP_03.ordinal();
    }

}
