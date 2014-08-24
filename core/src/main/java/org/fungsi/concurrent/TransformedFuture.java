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

final class TransformedFuture<T, R> implements Future<R> {
    private volatile Future<R> result;
    private final CountDownLatch resultSyn = new CountDownLatch(1);

    private List<Consumer<Either<R, Throwable>>> responders = new ArrayList<>();

    TransformedFuture(Future<T> parent, Function<Either<T, Throwable>, Future<R>> fn) {
        parent.respond(e -> {
            result = fn.apply(e);
            resultSyn.countDown();

            responders.forEach(result::respond);
            responders = null;
        });
    }

    @Override
    public Optional<Either<R, Throwable>> poll() {
        if (result == null) {
            return Optional.empty();
        }
        return result.poll();
    }

    @Override
    public R get() {
        try {
            resultSyn.await();
            return result.get();
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public R get(Duration timeout) {
        try {
            if (!resultSyn.await(timeout.toNanos(), TimeUnit.NANOSECONDS)) {
                throw new TimeoutException(timeout.toString());
            }
            return result.get(timeout);
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void respond(Consumer<Either<R, Throwable>> fn) {
        if (result != null) {
            result.respond(fn);
        } else {
            responders.add(fn);
        }
    }

    @Override
    public <TT> Future<TT> transform(Function<Either<R, Throwable>, Future<TT>> fn) {
        return new TransformedFuture<>(this, fn);
    }
}