package org.fungsi.concurrent;

import org.fungsi.Either;
import org.fungsi.Throwables;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

final class PromiseImpl<T> implements Promise<T> {
    private volatile Either<T, Throwable> result;
    private final CountDownLatch resultSyn = new CountDownLatch(1);

    private List<Consumer<Either<T, Throwable>>> responders = new ArrayList<>();

    @Override
    public Optional<Either<T, Throwable>> poll() {
        return Optional.ofNullable(result);
    }

    @Override
    public T get() {
        try {
            resultSyn.await();
            return Either.unsafe(result);
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public T get(Duration timeout) {
        try {
            if (!resultSyn.await(timeout.toNanos(), TimeUnit.NANOSECONDS)) {
                throw new TimeoutException(timeout.toString());
            }
            return Either.unsafe(result);
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void set(Either<T, Throwable> e) {
        if (this.result != null) {
            return;
        }

        this.result = e;
        this.resultSyn.countDown();

        responders.forEach(x -> x.accept(e));
        responders = null;
    }

    @Override
    public void respond(Consumer<Either<T, Throwable>> fn) {
        if (result != null) {
            fn.accept(result);
        } else {
            responders.add(fn);
        }
    }

    @Override
    public <TT> Future<TT> transform(Function<Either<T, Throwable>, Future<TT>> fn) {
        return new TransformedFuture<>(this, fn);
    }
}