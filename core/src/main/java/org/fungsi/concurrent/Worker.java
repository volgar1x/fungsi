package org.fungsi.concurrent;

import org.fungsi.Unit;
import org.fungsi.function.UnsafeRunnable;
import org.fungsi.function.UnsafeSupplier;

public interface Worker {
	<T> Future<T> submit(UnsafeSupplier<T> fn);

	default Future<Unit> cast(UnsafeRunnable fn) {
		return submit(() -> {
			fn.run();
			return Unit.instance();
		});
	}
}
