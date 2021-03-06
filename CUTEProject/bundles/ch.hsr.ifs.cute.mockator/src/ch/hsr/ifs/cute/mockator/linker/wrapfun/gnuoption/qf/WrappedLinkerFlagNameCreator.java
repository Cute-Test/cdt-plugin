package ch.hsr.ifs.cute.mockator.linker.wrapfun.gnuoption.qf;

public class WrappedLinkerFlagNameCreator {

    private static final String GNU_CPP_LINKER_WRAP_OPTION = "-wrap=";
    private final String        wrapFunName;

    public WrappedLinkerFlagNameCreator(final String wrapFunName) {
        this.wrapFunName = wrapFunName;
    }

    public String getWrappedLinkerFlagName() {
        return GNU_CPP_LINKER_WRAP_OPTION + wrapFunName;
    }
}
