package org.fungsi.concurrent;

import org.fungsi.Either;
import org.fungsi.Unit;
import org.fungsi.function.UnsafeRunnable;
import org.fungsi.function.UnsafeSupplier;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class Timers {
	private Timers() {}

	public static Timer wrap(ScheduledExecutorService executor) {
		return new TimerImpl(executor);
	}

    static class TimerImpl implements Timer {
        private final ScheduledExecutorService executor;

        TimerImpl(ScheduledExecutorService executor) {
            this.executor = executor;
        }

        @Override
        public <T> Future<T> flatSchedule(Duration duration, UnsafeSupplier<Future<T>> fn) {
            Promise<T> p = Promises.create();

            executor.schedule(() -> Futures.flatten(fn.safelyGet()).pipeTo(p),
                    duration.toNanos(),
                    TimeUnit.NANOSECONDS);

            return p;
        }

        @Override
        public <T> Future<T> schedule(Duration interval, UnsafeSupplier<T> fn) {
            Promise<T> p = Promises.create();

            executor.schedule(() -> {
                Either<T, Throwable> result = fn.safelyGet();
                p.set(result);
            }, interval.toNanos(), TimeUnit.NANOSECONDS);

            return p;
        }

        @Override
        public Future<Unit> schedule(Duration interval, UnsafeRunnable fn) {
            Promise<Unit> p = Promises.create();

            executor.schedule(() -> {
                Either<Unit, Throwable> result = fn.safelyRun();
                p.set(result);
            }, interval.toNanos(), TimeUnit.NANOSECONDS);

            return p;
        }
    }
}
