package org.fungsi.concurrent;

import org.fungsi.Either;
import org.fungsi.Throwables;

import java.time.Duration;
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

        if (r == null) {
            r = this.result = transformer.apply(e);
            notifyAll();
        }

        return r;
    }

    @Override
    public Optional<Either<R, Throwable>> poll() {
        return parent.poll().map(this::getResult)
                .flatMap(Future::poll);
    }

    @Override
    public synchronized R get() {
        try {
            while (result == null) {
                wait();
            }
        } catch (InterruptedException ignored) { }

        return result.get();
    }

    @Override
    public synchronized R get(Duration timeout) {
        try {
            long millis = timeout.toMillis();
            long start;
            while (result == null) {
                if (millis <= 0) {
                    throw new TimeoutException();
                }
                start = System.currentTimeMillis();
                wait();
                millis -= System.currentTimeMillis() - start;
            }
            return result.get(Duration.ofMillis(millis));
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
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