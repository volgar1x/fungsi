package org.fungsi.concurrent;

import org.fungsi.Unit;
import org.fungsi.function.UnsafeRunnable;
import org.fungsi.function.UnsafeSupplier;

import java.time.Duration;

public interface Timer {
	<T> Future<T> schedule(Duration interval, UnsafeSupplier<T> fn);

	default Future<Unit> schedule(Duration interval, UnsafeRunnable fn) {
		return schedule(interval, () -> {
			fn.run();
			return Unit.instance();
		});
	}
}
