package org.fungsi.concurrent;

import org.fungsi.Either;
import org.fungsi.function.UnsafeFunction;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

final class PromiseImpl<T> implements Promise<T> {
	private Optional<Either<T, Throwable>> opt = Optional.empty();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Deque<Consumer<Either<T, Throwable>>> listeners = new LinkedList<>();
    private final Lock listenersLock = new ReentrantLock();

	@Override
	public void set(Either<T, Throwable> e) {
		if (poll().isPresent()) return;

		lock.writeLock().lock();
		try {
			this.opt = Optional.of(e);
		} finally {
			lock.writeLock().unlock();
		}

        listenersLock.lock();
		try {
            while (!listeners.isEmpty()) {
                listeners.removeFirst().accept(e);
            }
        } finally {
            listenersLock.unlock();
        }
    }

	@Override
	public Optional<Either<T, Throwable>> poll() {
		lock.readLock().lock();
		try {
			return opt;
		} finally {
			lock.readLock().unlock();
		}
	}
	@Override
	public T get() {
		Either<T, Throwable> result;

		while (true) {
			Optional<Either<T, Throwable>> value = poll();

			if (value.isPresent()) {
				result = value.get();
				break;
			} else {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					throw new Error(e);
				}
			}
		}

		return Either.unsafe(result);
	}

	@Override
	public T get(Duration timeout) {
		Instant deadline = Instant.now().plus(timeout);
		Either<T, Throwable> result;

		while (true) {
			Optional<Either<T, Throwable>> value = poll();

			if (value.isPresent()) {
				result = value.get();
				break;
			} else {
				if (Instant.now().isAfter(deadline)) {
					throw new TimeoutException();
				}
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					throw new Error(e);
				}
			}
		}

		return Either.unsafe(result);
	}

	@Override
	public Future<T> respond(Consumer<Either<T, Throwable>> fn) {
        listenersLock.lock();
		try {
            listeners.addFirst(fn);
        } finally {
            listenersLock.unlock();
        }
        return this;
    }

	@Override
	public <TT> Future<TT> bind(UnsafeFunction<T, Future<TT>> fn) {
		return new BoundFuture<>(this, fn);
	}

	@Override
	public String toString() {
		return "Promise(" +
				(isDone()
					? isSuccess()
						? "success"
						: "failure"
					: "pending") +
				")";
	}
}
