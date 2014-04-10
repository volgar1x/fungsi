package org.fungsi.function;

@FunctionalInterface
public interface UnsafePredicate<T> {
	boolean test(T o) throws Throwable;
}
