package org.fungsi.concurrent;

import org.fungsi.Either;
import org.fungsi.Unit;
import org.fungsi.function.UnsafeRunnable;
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

		@Override
		public Future<Unit> cast(UnsafeRunnable fn) {
			Promise<Unit> promise = Promises.create();
			executor.execute(() -> {
				try {
					fn.run();
					promise.set(Unit.left());
				} catch (Throwable cause) {
					promise.set(Either.failure(cause));
				}
			});
			return promise;
		}
	}
}
