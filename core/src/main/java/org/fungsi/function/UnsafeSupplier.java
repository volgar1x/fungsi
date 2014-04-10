package org.fungsi.function;

import org.fungsi.Either;

import java.util.function.Supplier;

@FunctionalInterface
public interface UnsafeSupplier<T> {
	T get() throws Throwable;

	default Supplier<Either<T, Throwable>> safe() {
		return () -> {
			try {
				return Either.success(get());
			} catch (Throwable cause) {
				return Either.failure(cause);
			}
		};
	}

	default Supplier<T> unsafe() {
		return () -> {
			try {
				return get();
			} catch (Error|RuntimeException e) {
				throw e;
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		};
	}

	default Either<T, Throwable> safelyGet() {
		return safe().get();
	}
}
