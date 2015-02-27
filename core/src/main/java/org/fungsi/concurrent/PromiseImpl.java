package org.fungsi.concurrent;

import org.fungsi.Either;
import org.fungsi.Throwables;

import java.time.Duration;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

final class PromiseImpl<T> implements Promise<T> {
    volatile Either<T, Throwable> result = null;
    final Deque<Consumer<Either<T, Throwable>>> buffer = new LinkedList<>();

    @Override
    public Optional<Either<T, Throwable>> poll() {
        return Optional.ofNullable(result);
    }

    @Override
    public synchronized T get() {
        try {
            wait();
            return Either.unsafe(result);
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public synchronized T get(Duration timeout) {
        try {
            wait(timeout.toMillis());
            return Either.unsafe(result);
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public synchronized void set(Either<T, Throwable> e) {
        if (this.result != null) {
            return;
        }

        this.result = e;
        notifyAll();

        while (!buffer.isEmpty()) {
            buffer.removeFirst().accept(e);
        }
    }

    @Override
    public synchronized void respond(Consumer<Either<T, Throwable>> fn) {
        Either<T, Throwable> e = this.result;
        if (e != null) {
            fn.accept(e);
        } else {
            buffer.addLast(fn);
        }
    }

    @Override
    public <TT> Future<TT> transform(Function<Either<T, Throwable>, Future<TT>> fn) {
        return new TransformedFuture<>(this, fn);
    }

    @Override
    public String toString() {
        Either<T, Throwable> e = this.result;
        String state;
        if (e == null) {
            state = "pending";
        } else if (e.isRight()) {
            state = "failed";
        } else {
            state = "success";
        }
        return "Promise(" + state + ")";
    }
}