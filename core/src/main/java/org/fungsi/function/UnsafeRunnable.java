package org.fungsi.function;

@FunctionalInterface
public interface UnsafeRunnable {
	void run() throws Throwable;

	default UnsafeRunnable then(UnsafeRunnable other) {
		return () -> {
			run();
			other.run();
		};
	}
}
