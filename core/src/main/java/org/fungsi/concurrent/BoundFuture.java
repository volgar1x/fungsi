package org.fungsi.concurrent;

import org.fungsi.Either;

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
import java.util.function.Function;

final class BoundFuture<From, To> implements Future<To> {
	private Optional<Future<To>> to = Optional.empty();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Deque<Consumer<Either<To, Throwable>>> listeners = new LinkedList<>();
    private final Lock listenersLock = new ReentrantLock();

	BoundFuture(Future<From> from, Function<Either<From, Throwable>, Future<To>> fn) {
		from.respond(f -> {
            Future<To> fut = fn.apply(f);

            listenersLock.lock();
            try {
                while (!listeners.isEmpty()) {
                    fut.respond(listeners.removeFirst());
                }
            } finally {
                listenersLock.unlock();
            }

            lock.writeLock().lock();
            try {
                to = Optional.of(fut);
            } finally {
                lock.writeLock().unlock();
            }
        });
	}

	@Override
	public Optional<Either<To, Throwable>> poll() {
		lock.readLock().lock();
		try {
			return to.flatMap(Future::poll);
		} finally {
			lock.readLock().unlock();
		}
	}

	Future<To> waitForIt() {
		while (true) {
			lock.readLock().lock();
			try {
				if (to.isPresent()) {
					return to.get();
				}
			} finally {
				lock.readLock().unlock();
			}

			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				throw new Error(e);
			}
		}
	}

	Future<To> waitForIt(Duration timeout) {
		Instant deadline = Instant.now().plus(timeout);
		while (true) {
			lock.readLock().lock();
			try {
				if (to.isPresent()) {
					return to.get();
				}
			} finally {
				lock.readLock().unlock();
			}

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

	@Override
	public To get() {
		return waitForIt().get();
	}

	@Override
	public To get(Duration timeout) {
		Instant deadline = Instant.now().plus(timeout);
		Future<To> fut = waitForIt(timeout);
		return fut.get(Duration.between(Instant.now(), deadline));
	}

	@Override
	public void respond(Consumer<Either<To, Throwable>> fn) {
		lock.readLock().lock();
		try {
			if (to.isPresent()) {
				to.get().respond(fn);
				return;
			}
		} finally {
			lock.readLock().unlock();
		}

        listenersLock.lock();
		try {
            listeners.addFirst(fn);
        } finally {
            listenersLock.unlock();
        }
	}

    @Override
    public <TT> Future<TT> transform(Function<Either<To, Throwable>, Future<TT>> fn) {
        return new BoundFuture<>(this, fn);
    }
}
