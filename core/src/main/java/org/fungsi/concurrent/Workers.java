package org.fungsi.concurrent;

import org.fungsi.function.UnsafeSupplier;

import java.util.concurrent.Executor;

public final class Workers {
	private Workers() {}

	public static Worker wrap(Executor executor) {
		return new WrapExecutorService(executor);
	}

	static final class WrapExecutorService implements Worker {
		final Executor executor;

		WrapExecutorService(Executor executor) {
			this.executor = executor;
		}

		@Override
		public <T> Future<T> submit(UnsafeSupplier<T> fn) {
			Promise<T> promise = Promises.create();
			executor.execute(() -> promise.set(fn.safelyGet()));
			return promise;
		}
	}
}
