package org.fungsi.function;

@FunctionalInterface
public interface UnsafePredicate<T> {
	boolean test(T o) throws Throwable;

	default UnsafePredicate<T> and(UnsafePredicate<T> other) {
		return it -> test(it) && other.test(it);
	}

	default UnsafePredicate<T> or(UnsafePredicate<T> other) {
		return it -> test(it) || other.test(it);
	}

	default UnsafePredicate<T> not() {
		return it -> !test(it);
	}
}
