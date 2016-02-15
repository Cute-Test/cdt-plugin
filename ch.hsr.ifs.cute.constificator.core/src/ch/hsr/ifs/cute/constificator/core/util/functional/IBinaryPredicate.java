package ch.hsr.ifs.cute.constificator.core.util.functional;

public interface IBinaryPredicate<T1, T2> {
	public boolean holdsFor(T1 ancestor, T2 reference);
}
