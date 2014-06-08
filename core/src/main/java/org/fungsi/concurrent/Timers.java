package org.fungsi.concurrent;

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
        public <T> Future<T> schedule(Duration interval, UnsafeSupplier<T> fn) {
            Promise<T> p = Promises.create();
            executor.schedule(() -> p.set(fn.safelyGet()), interval.toNanos(), TimeUnit.NANOSECONDS);
            return p;
        }
    }
}
