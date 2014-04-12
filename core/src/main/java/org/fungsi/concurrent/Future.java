package org.fungsi.concurrent;

import org.fungsi.Either;
import org.fungsi.Unit;
import org.fungsi.function.UnsafeFunction;
import org.fungsi.function.UnsafePredicate;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;

public interface Future<T> {
	public static <T> Future<T> constant(Either<T, Throwable> e) { return new ConstFuture<>(e); }
	@SuppressWarnings("unchecked")
	public static <T> Future<T> never() { return (Future<T>) NEVER; }

	T get();
	T get(Duration timeout);
	Optional<Either<T, Throwable>> poll();

	Future<T> respond(Consumer<Either<T, Throwable>> fn);
	<TT> Future<TT> bind(UnsafeFunction<T, Future<TT>> fn);

	default boolean isDone() {
		return poll().isPresent();
	}

	default boolean isSuccess() {
		return poll().map(Either::isLeft).orElse(false);
	}

	default boolean isFailure() {
		return poll().map(Either::isRight).orElse(false);
	}

	default <TT> Future<TT> flatMap(UnsafeFunction<T, Future<TT>> fn) { return bind(fn); }

	default <TT> Future<TT> map(UnsafeFunction<T, TT> fn) {
		return bind(fn.eitherFunction().andThen(Future::constant));
	}

	default Future<T> filter(UnsafePredicate<T> fn) {
		return bind(o -> fn.test(o) ? Future.this : never());
	}

	default Future<T> then(Future<T> other) {
		return bind(it -> other);
	}

	default Future<Unit> toUnit() {
		return bind(it -> Futures.unit());
	}

	default Future<T> pipeTo(Promise<T> p) {
		return respond(p::set);
	}

	default Future<T> within(Duration d, Timer timer) {
		Promise<T> p = Promises.create();

		timer.schedule(d, () -> p.set(Either.failure(new TimeoutException())));
		pipeTo(p);

		return p;
	}

	default Future<T> onSuccess(Consumer<T> fn) {
		return respond(e -> e.ifLeft(fn));
	}

	default Future<T> onFailure(Consumer<Throwable> fn) {
		return respond(e -> e.ifRight(fn));
	}

	static final class ConstFuture<T> implements Future<T> {

		private final Either<T, Throwable> e;

		public ConstFuture(Either<T, Throwable> e) {
			this.e = e;
		}

		@Override
		public Optional<Either<T, Throwable>> poll() {
			return Optional.of(e);
		}

		@Override
		public T get() {
			return Either.unsafe(e);
		}

		@Override
		public T get(Duration timeout) {
			return get();
		}

		@Override
		public Future<T> respond(Consumer<Either<T, Throwable>> fn) {
			fn.accept(e);
			return this;
		}

		@Override
		public <TT> Future<TT> bind(UnsafeFunction<T, Future<TT>> fn) {
			return e.fold(Futures.safe(fn), r -> never());
		}
	}

	static final Future<Object> NEVER = new Future<Object>() {

		@SuppressWarnings("unchecked")
		private <TT> Future<TT> self() { return (Future<TT>) this; }

		@Override
		public Optional<Either<Object, Throwable>> poll() {
			return Optional.empty();
		}

		@Override
		public Object get() {
			throw new IllegalStateException();
		}

		@Override
		public Object get(Duration timeout) {
			throw new TimeoutException();
		}

		@Override
		public boolean isDone() {
			return false;
		}

		@Override
		public boolean isSuccess() {
			return false;
		}

		@Override
		public boolean isFailure() {
			return false;
		}

		@Override
		public Future<Object> respond(Consumer<Either<Object, Throwable>> fn) {
			return this;
		}

		@Override
		public <TT> Future<TT> bind(UnsafeFunction<Object, Future<TT>> fn) {
			return self();
		}

		@Override
		public <TT> Future<TT> map(UnsafeFunction<Object, TT> fn) {
			return self();
		}

		@Override
		public Future<Object> filter(UnsafePredicate<Object> fn) {
			return self();
		}
	};
}
