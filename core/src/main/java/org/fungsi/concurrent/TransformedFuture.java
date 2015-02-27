package org.fungsi.concurrent;

import org.fungsi.Either;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

final class TransformedFuture<T, R> implements Future<R> {
    final Future<T> parent;
    final Function<Either<T, Throwable>, Future<R>> transformer;

    volatile Future<R> result;

    TransformedFuture(Future<T> parent, Function<Either<T, Throwable>, Future<R>> transformer) {
        this.parent = parent;
        this.transformer = transformer;

        this.parent.respond(this::getResult);
    }

    synchronized Future<R> getResult(Either<T, Throwable> e) {
        Future<R> r = this.result;
        if (r != null) {
            return r;
        }
        return this.result = transformer.apply(e);
    }

    @Override
    public Optional<Either<R, Throwable>> poll() {
        return parent.poll().map(this::getResult)
                .flatMap(Future::poll);
    }

    @Override
    public synchronized R get() {
        if (result == null) {
            try {
                wait();
            } catch (InterruptedException ignored) { }
        }
        return result.get();
    }

    @Override
    public synchronized R get(Duration timeout) {
        if (result == null) {
            Instant start = Instant.now();
            try {
                wait(timeout.toMillis());
            } catch (InterruptedException ignored) { }
            Instant end = Instant.now();

            return result.get(timeout.minus(Duration.between(start, end)));
        } else {
            return result.get(timeout);
        }
    }

    @Override
    public void respond(Consumer<Either<R, Throwable>> fn) {
        parent.respond(e -> {
            Future<R> f = getResult(e);
            f.respond(fn);
        });
    }

    @Override
    public <TT> Future<TT> transform(Function<Either<R, Throwable>, Future<TT>> fn) {
        return new TransformedFuture<>(this, fn);
    }
}