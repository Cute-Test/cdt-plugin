package ch.hsr.ifs.cute.mockator.testdouble.support;

import java.util.Collection;

import ch.hsr.ifs.iltis.core.resources.StringUtil;


public abstract class MemFunSignature implements Comparable<MemFunSignature> {

    private static final String REGEX_PREFIX = "^";
    private String              funSignature;

    /**
     * Default constructor for IStringifyable
     */
    public MemFunSignature() {}

    public MemFunSignature(final String funSignature) {
        setFunSignature(funSignature);
    }

    protected void setFunSignature(final String funSignature) {
        this.funSignature = StringUtil.unquote(funSignature);
    }

    public String getMemFunSignature() {
        return funSignature;
    }

    @Override
    public String toString() {
        return funSignature;
    }

    @Override
    public int hashCode() {
        return funSignature.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) return true;

        if (!(obj instanceof MemFunSignature)) return false;

        final MemFunSignature other = (MemFunSignature) obj;
        return funSignature.equals(other.funSignature);
    }

    @Override
    public int compareTo(final MemFunSignature o) {
        return funSignature.compareTo(o.funSignature);
    }

    public boolean isCovered(final Collection<? extends MemFunSignature> signatures) {
        if (signatures.contains(this)) return true;

        // we cannot match by using regular expressions because they are
        // dependent on the function arguments; therefore we just use the rule that
        // everything matches as soon as the user has an expectation with a regex
        if (funSignature.startsWith(REGEX_PREFIX)) return true;

        for (final MemFunSignature sig : signatures) {
            if (sig.funSignature.startsWith(REGEX_PREFIX)) return true;
        }

        return false;
    }

}
