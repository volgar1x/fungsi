package org.fungsi.function;

import org.fungsi.Either;

import java.util.function.Function;
import java.util.function.Supplier;

public final class Functions {
	private Functions() {}

	public static <T, R> Function<T, R> thenThrow(Supplier<? extends Throwable> fn) {
		return it -> {
			Throwable t = fn.get();
			if (t instanceof Error) {
				throw (Error) t;
			} else if (t instanceof RuntimeException) {
				throw (RuntimeException) t;
			} else {
				throw new RuntimeException(t);
			}
		};
	}

    public static <T> Either<T, Throwable> safe(UnsafeSupplier<T> fn) {
        return fn.safeSupplier().get();
    }
}
