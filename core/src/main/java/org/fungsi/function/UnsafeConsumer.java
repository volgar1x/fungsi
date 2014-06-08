package org.fungsi.function;

import org.fungsi.Either;
import org.fungsi.Throwables;
import org.fungsi.Unit;

import java.util.function.Consumer;
import java.util.function.Function;

@FunctionalInterface
public interface UnsafeConsumer<T> {
    void accept(T t) throws Throwable;

    default UnsafeConsumer<T> andThen(UnsafeConsumer<T> other) {
        return param -> {
            this.accept(param);
            other.accept(param);
        };
    }

    default Function<T, Either<Unit, Throwable>> safeConsumer() {
        return param -> {
            try {
                accept(param);
                return Unit.left();
            } catch (Throwable cause) {
                return Either.failure(cause);
            }
        };
    }

    default Consumer<T> unsafeConsumer() {
        return param -> {
            try {
                accept(param);
            } catch (Throwable t) {
                throw Throwables.propagate(t);
            }
        };
    }
}
