package org.fungsi.concurrent;

import org.fungsi.Either;
import org.fungsi.Unit;
import org.fungsi.function.UnsafeRunnable;
import org.fungsi.function.UnsafeSupplier;

import java.util.concurrent.Executor;

public final class Workers {
	private Workers() {}

	public static Worker wrap(Executor executor) {
		return new WrapExecutor(executor);
	}

	static final class WrapExecutor implements Worker {
        private final Executor executor;

        WrapExecutor(Executor executor) {
            this.executor = executor;
        }

        @Override
        public <T> Future<T> execute(UnsafeSupplier<Future<T>> fn) {
            Promise<T> promise = new PromiseImpl<>();
            executor.execute(() -> {
                Future<T> result = Futures.flatten(fn.safelyGet());
                result.pipeTo(promise);
            });
            return promise;
        }

        @Override
        public <T> Future<T> submit(UnsafeSupplier<T> fn) {
            Promise<T> promise = new PromiseImpl<>();
            executor.execute(() -> {
                Either<T, Throwable> result = fn.safelyGet();
                promise.set(result);
            });
            return promise;
        }

        @Override
        public Future<Unit> cast(UnsafeRunnable fn) {
            Promise<Unit> promise = new PromiseImpl<>();
            executor.execute(() -> {
                Either<Unit, Throwable> result = fn.safelyRun();
                promise.set(result);
            });
            return promise;
        }
	}
}
