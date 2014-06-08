package org.fungsi.function;

import org.fungsi.Either;
import org.fungsi.Throwables;

import java.util.function.Supplier;

@FunctionalInterface
public interface UnsafeSupplier<T> {
	T get() throws Throwable;

	default Supplier<Either<T, Throwable>> safeSupplier() {
		return () -> {
			try {
				return Either.success(get());
			} catch (Throwable cause) {
				return Either.failure(cause);
			}
		};
	}

	default Supplier<T> unsafeSupplier() {
		return () -> {
			try {
				return get();
			} catch (Throwable t) {
				throw Throwables.propagate(t);
			}
		};
	}

	default Either<T, Throwable> safelyGet() {
		return safeSupplier().get();
	}
}
