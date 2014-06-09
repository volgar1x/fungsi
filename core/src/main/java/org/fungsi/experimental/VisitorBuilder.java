package org.fungsi.experimental;

import org.fungsi.Throwables;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Deprecated
public final class VisitorBuilder<T, R> {
    private Function<? super T, Optional<? extends R>> result = param -> Optional.empty();

    public static <T, R> VisitorBuilder<T, R> start() {
        return new VisitorBuilder<>();
    }

    @SuppressWarnings("UnusedParameters")
    public static <T, R> VisitorBuilder<T, R> start(Class<T> ignoredT, Class<R> ignoredR) {
        return start();
    }

    @SuppressWarnings("UnusedParameters")
    public static <T, R> VisitorBuilder<T, R> visitor(Class<T> ignoredT, Class<R> ignoredR) {
        return start();
    }

    private VisitorBuilder<T, R> decorate(Function<Function<? super T, Optional<? extends R>>, Function<? super T, Optional<? extends R>>> fn) {
        result = fn.apply(result);
        return this;
    }

    public VisitorBuilder<T, R> matchIf(Predicate<? super T> filter, Function<? super T, Optional<? extends R>> fn) {
        return decorate(old -> param -> {
            if (filter.test(param)) {
                return fn.apply(param);
            }
            return old.apply(param);
        });
    }

    public <A extends T> VisitorBuilder<T, R> matchClass(Class<A> klass, Function<A, Optional<? extends R>> fn) {
        return matchIf(klass::isInstance, fn.compose(klass::cast));
    }

    public <A extends T> VisitorBuilder<T, R> matchEqual(A instance, Function<A, Optional<? extends R>> fn) {
        return matchIf(instance::equals, fn.compose(param -> instance));
    }

    public VisitorBuilder<T, R> ifNull(Supplier<Optional<? extends R>> fn) {
        return matchIf(param -> param == null, param -> fn.get());
    }

    public Function<? super T, Optional<? extends R>> asFunction() {
        return result;
    }

    public Function<? super T, ? extends R> otherwise(Supplier<? extends R> fn) {
        return param -> {
            Optional<? extends R> opt = result.apply(param);
            if (opt.isPresent()) {
                return opt.get();
            } else {
                return fn.get();
            }
        };
    }

    public Function<? super T, ? extends R> orFail(Supplier<Throwable> fn) {
        return param -> {
            Optional<? extends R> opt = result.apply(param);
            if (opt.isPresent()) {
                return opt.get();
            } else {
                throw Throwables.propagate(fn.get());
            }
        };
    }
}
