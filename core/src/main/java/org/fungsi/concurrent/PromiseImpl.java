package org.fungsi.concurrent;

import org.fungsi.Either;
import org.fungsi.Throwables;

import java.time.Duration;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;
import java.util.function.Function;

final class PromiseImpl<T> implements Promise<T> {

    private Either<T, Throwable> result;
    private final StampedLock resultLock = new StampedLock();
    private final CountDownLatch resultLatch = new CountDownLatch(1);

    private Deque<Consumer<Either<T, Throwable>>> responders = new ConcurrentLinkedDeque<>();
    private final StampedLock respondersLock = new StampedLock();

    @Override
    public Optional<Either<T, Throwable>> poll() {
        long stamp = resultLock.tryOptimisticRead();
        Either<T, Throwable> result = this.result;
        if (resultLock.validate(stamp)) {
            return Optional.ofNullable(result);
        }
        stamp = resultLock.readLock();
        try {
            return Optional.ofNullable(result);
        } finally {
            resultLock.unlockRead(stamp);
        }
    }

    @Override
    public T get() {
        try {
            resultLatch.await();
            return Either.unsafe(result);
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public T get(Duration timeout) {
        try {
            if (!resultLatch.await(timeout.toNanos(), TimeUnit.NANOSECONDS)) {
                throw new TimeoutException(timeout.toString());
            }
            return Either.unsafe(result);
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void set(Either<T, Throwable> result) {
        if (this.result != null) {
            return;
        }

        long stamp;

        stamp = resultLock.writeLock();
        try {
            if (this.result != null) {
                return;
            }
            this.result = result;
        } finally {
            resultLock.unlockWrite(stamp);
        }

        resultLatch.countDown();

        stamp = respondersLock.writeLock();
        try {
            while (!responders.isEmpty()) {
                responders.pollFirst().accept(result);
            }
        } finally {
            respondersLock.unlockWrite(stamp);
        }
    }

    @Override
    public void respond(Consumer<Either<T, Throwable>> fn) {
        Deque<Consumer<Either<T, Throwable>>> responders;

        responders = this.responders;
        if (responders == null) {
            fn.accept(result);
            return;
        }

        long stamp = respondersLock.tryOptimisticRead();
        responders = this.responders;
        if (respondersLock.validate(stamp)) {
            if (responders == null) {
                fn.accept(result);
            } else {
                responders.addLast(fn);
            }
            return;
        }

        stamp = respondersLock.readLock();
        respondersLock.unlockRead(stamp);

        fn.accept(result);
    }

    @Override
    public <TT> Future<TT> transform(Function<Either<T, Throwable>, Future<TT>> fn) {
        return new TransformedFuture<>(this, fn);
    }

    @Override
    public String toString() {
        return "PromiseImpl(" + poll() + ")";
    }
}