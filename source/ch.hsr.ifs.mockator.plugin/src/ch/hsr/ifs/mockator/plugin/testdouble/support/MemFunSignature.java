package ch.hsr.ifs.mockator.plugin.testdouble.support;

import java.util.Collection;

import ch.hsr.ifs.mockator.plugin.base.util.StringUtil;

public abstract class MemFunSignature implements Comparable<MemFunSignature> {
  private static final String REGEX_PREFIX = "^";
  private final String funSignature;

  public MemFunSignature(String funSignature) {
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
  public boolean equals(Object obj) {
    if (obj == this)
      return true;

    if (!(obj instanceof MemFunSignature))
      return false;

    MemFunSignature other = (MemFunSignature) obj;
    return funSignature.equals(other.funSignature);
  }

  @Override
  public int compareTo(MemFunSignature o) {
    return funSignature.compareTo(o.funSignature);
  }

  public boolean isCovered(Collection<? extends MemFunSignature> signatures) {
    if (signatures.contains(this))
      return true;

    // we cannot match by using regular expressions because they are
    // dependent on the function arguments; therefore we just use the rule that
    // everything matches as soon as the user has an expectation with a regex
    if (funSignature.startsWith(REGEX_PREFIX))
      return true;

    for (MemFunSignature sig : signatures) {
      if (sig.funSignature.startsWith(REGEX_PREFIX))
        return true;
    }

    return false;
  }
}
