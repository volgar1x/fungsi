package org.fungsi.function;

import org.fungsi.Either;
import org.fungsi.Unit;

import java.util.function.Supplier;

@FunctionalInterface
public interface UnsafeRunnable {
	void run() throws Throwable;

	default UnsafeRunnable andThen(UnsafeRunnable other) {
		return () -> {
			run();
			other.run();
		};
	}

	default Supplier<Either<Unit, Throwable>> safe() {
		return () -> {
			try {
				run();
				return Unit.left();
			} catch (Throwable cause) {
				return Either.right(cause);
			}
		};
	}

	default Runnable unsafe() {
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
		return safe().get();
	}

	static final UnsafeRunnable NOOP = () -> {};

	public static UnsafeRunnable noop() {
		return NOOP;
	}
}
