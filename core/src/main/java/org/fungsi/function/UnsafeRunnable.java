package org.fungsi.function;

import org.fungsi.Either;
import org.fungsi.Unit;

import java.util.function.Supplier;

@FunctionalInterface
public interface UnsafeRunnable {
	void run() throws Throwable;

	default UnsafeRunnable then(UnsafeRunnable other) {
		return () -> {
			run();
			other.run();
		};
	}

	default <T> UnsafeSupplier<T> thenReturn(T o) {
		return () -> {
			run();
			return o;
		};
	}

	default Supplier<Either<Unit, Throwable>> safeRunnable() {
		return () -> {
			try {
				run();
				return Unit.left();
			} catch (Throwable cause) {
				return Either.right(cause);
			}
		};
	}

	default Runnable unsafeRunnable() {
		return () -> {
			try {
				run();
			} catch (Error|RuntimeException e) {
				throw e;
			} catch (Throwable cause) {
				throw new RuntimeException(cause);
			}
		};
	}

	default Either<Unit, Throwable> safelyRun() {
		return safeRunnable().get();
	}

	static final UnsafeRunnable NOOP = () -> {};

	public static UnsafeRunnable noop() {
		return NOOP;
	}
}
