package org.fungsi.concurrent;

import org.fungsi.Unit;
import org.fungsi.function.UnsafeRunnable;
import org.fungsi.function.UnsafeSupplier;

public interface Worker {
    <T> Future<T> execute(UnsafeSupplier<Future<T>> fn);

	<T> Future<T> submit(UnsafeSupplier<T> fn);

	Future<Unit> cast(UnsafeRunnable fn);
}
