package org.fungsi.function;

import org.fungsi.Either;
import org.fungsi.Throwables;

import java.util.function.Function;
import java.util.function.Predicate;

@FunctionalInterface
public interface UnsafePredicate<T> {
	boolean test(T o) throws Throwable;

	default UnsafePredicate<T> and(UnsafePredicate<T> other) {
		return it -> test(it) && other.test(it);
	}

	default UnsafePredicate<T> or(UnsafePredicate<T> other) {
		return it -> test(it) || other.test(it);
	}

	default UnsafePredicate<T> not() {
		return it -> !test(it);
	}

    default Function<T, Either<Boolean, Throwable>> safePredicate() {
        return param -> {
            try {
                return Either.success(test(param));
            } catch (Throwable throwable) {
                return Either.failure(throwable);
            }
        };
    }

    default Predicate<T> unsafePredicate() {
        return param -> {
            try {
                return test(param);
            } catch (Throwable throwable) {
                throw Throwables.propagate(throwable);
            }
        };
    }
}
