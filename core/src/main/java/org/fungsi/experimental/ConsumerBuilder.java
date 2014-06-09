package org.fungsi.experimental;

import org.fungsi.Either;
import org.fungsi.Unit;
import org.fungsi.function.UnsafeRunnable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.fungsi.Either.failure;

@Deprecated
public final class ConsumerBuilder<T> {
    private ConsumerBuilder() {}

    public static <T> ConsumerBuilder<T> start() {
        return new ConsumerBuilder<>();
    }

    public static <T> ConsumerBuilder<T> start(Class<T> ignored) {
        return start();
    }

    public static <T> ConsumerBuilder<T> consumer(Class<T> ignored) {
        return start();
    }

    @FunctionalInterface
    private interface Matcher<T> {
        Either<Unit, Throwable> accept(T param);
    }

    private Matcher<T> result = param -> failure(new IllegalArgumentException());

    private ConsumerBuilder<T> decorate(Function<Matcher<T>, Matcher<T>> fn) {
        result = fn.apply(result);
        return this;
    }

    public ConsumerBuilder<T> matchIf(Predicate<T> filter, Consumer<T> fn) {
        return decorate(old -> param -> {
            if (filter.test(param)) {
                fn.accept(param);
                return Unit.left();
            }
            return old.accept(param);
        });
    }

    public <A extends T> ConsumerBuilder<T> matchClass(Class<A> klass, Consumer<A> fn) {
        return matchIf(klass::isInstance, param -> fn.accept(klass.cast(param)));
    }

    public <A extends T> ConsumerBuilder<T> matchEqual(A instance, Consumer<A> fn) {
        return matchIf(instance::equals, param -> fn.accept(instance));
    }

    public ConsumerBuilder<T> ifNull(Runnable fn) {
        return matchIf(param -> param == null, param -> fn.run());
    }

    public Consumer<T> asConsumer() {
        return param -> Either.<Unit>unsafe(result.accept(param));
    }

    public Consumer<T> otherwise(UnsafeRunnable fn) {
        return param -> result.accept(param).leftFallback(fn::safelyRun);
    }

    public Consumer<T> orFail(Supplier<Throwable> fn) {
        return param -> result.accept(param).leftOrThrow(fn);
    }
}
