package org.fungsi.function;

import org.fungsi.Either;
import org.fungsi.Throwables;
import org.fungsi.Unit;

import java.util.function.Function;

@FunctionalInterface
public interface UnsafeFunction<T, R> {
	R apply(T o) throws Throwable;

	default Either<R, Throwable> safeApply(T o) {
		try {
			return Either.success(apply(o));
		} catch (Throwable cause) {
			return Either.failure(cause);
		}
	}

	default Function<T, Either<R, Throwable>> safeFunction() {
		return this::safeApply;
	}

	default Function<T, R> unsafeFunction() {
		return o -> {
			try {
				return apply(o);
			} catch (Throwable t) {
				throw Throwables.propagate(t);
			}
		};
	}

	default UnsafeFunction<T, Either<R, Throwable>> eitherFunction() {
		return this::safeApply;
	}

	default <TT> UnsafeFunction<TT, R> compose(UnsafeFunction<TT, T> other) {
		return o -> this.apply(other.apply(o));
	}

	default <RR> UnsafeFunction<T, RR> andThen(UnsafeFunction<R, RR> other) {
		return o -> other.apply(this.apply(o));
	}

	static final UnsafeFunction<Object, Object> IDENTITY = o -> o;
	static final UnsafeFunction<Object, Object> NEVER = o -> { throw new IllegalStateException(); };
	static final UnsafeFunction<Object, Unit> UNIT = o -> Unit.instance();

	@SuppressWarnings("unchecked")
	public static <T> UnsafeFunction<T, T> identity() {
		return (UnsafeFunction<T, T>) IDENTITY;
	}

	@SuppressWarnings("unchecked")
	public static <T, R> UnsafeFunction<T, R> never() {
		return (UnsafeFunction<T, R>) NEVER;
	}

	@SuppressWarnings("unchecked")
	public static <T> UnsafeFunction<T, Unit> unit() {
		return (UnsafeFunction<T, Unit>) UNIT;
	}
}
