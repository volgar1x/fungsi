package org.fungsi.experimental;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Deprecated
public final class VisitorBuilder<R> {
    private Function<Object, Optional<? extends R>> result = param -> Optional.empty();

    public <T> VisitorBuilder<R> match(Class<T> klass, Function<? super T, ? extends R> fn) {
        Function<Object, Optional<? extends R>> old = result;

        result = (Object param) -> {
            if (klass.isInstance(param)) {
                T tParam = klass.cast(param);
                return Optional.of(fn.apply(tParam));
            }
            return old.apply(param);
        };

        return this;
    }

    public <T> VisitorBuilder<R> match(T instance, Supplier<? extends R> fn) {
        Function<Object, Optional<? extends R>> old = result;

        result = (Object param) -> {
            if (instance.equals(param)) {
                return Optional.of(fn.get());
            }
            return old.apply(param);
        };

        return this;
    }

    public VisitorBuilder<R> ifNull(Supplier<? extends R> fn) {
        Function<Object, Optional<? extends R>> old = result;

        result = (Object param) -> {
            if (param == null) {
                return Optional.of(fn.get());
            }

            return old.apply(param);
        };

        return this;
    }

    public Function<Object, Optional<? extends R>> otherwise(Function<Object, ? extends R> fn) {
        return (Object param) -> {
            Optional<? extends R> opt = result.apply(param);
            if (opt.isPresent()) {
                return opt;
            }
            return Optional.of(fn.apply(param));
        };
    }
}
