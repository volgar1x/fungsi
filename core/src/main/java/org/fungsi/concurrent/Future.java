package org.fungsi.concurrent;

import org.fungsi.Either;
import org.fungsi.Unit;
import org.fungsi.function.UnsafeFunction;
import org.fungsi.function.UnsafePredicate;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Future<T> {
	public static <T> Future<T> constant(Either<T, Throwable> e) { return new ConstFuture<>(e); }
	@SuppressWarnings("unchecked")
	public static <T> Future<T> never() { return (Future<T>) NEVER; }

	T get();
	T get(Duration timeout);
	Optional<Either<T, Throwable>> poll();

	void respond(Consumer<Either<T, Throwable>> fn);
    <TT> Future<TT> transform(Function<Either<T, Throwable>, Future<TT>> fn);

	default <TT> Future<TT> bind(UnsafeFunction<T, Future<TT>> fn) {
        return transform(e -> e.fold(Futures.safe(fn), Futures::failure));
    }

	default boolean isDone() {
		return poll().isPresent();
	}

	default boolean isSuccess() {
		return isDone() && poll().get().isLeft();
	}

	default boolean isFailure() {
		return isDone() && poll().get().isRight();
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
		respond(p::set);
        return this;
	}

	default Future<T> within(Duration d, Timer timer) {
		Promise<T> p = Promises.create();

		timer.schedule(d, () -> p.set(Either.failure(new TimeoutException())));
		pipeTo(p);

		return p;
	}

	default Future<T> onSuccess(Consumer<T> fn) {
		respond(e -> e.ifLeft(fn));
        return this;
	}

	default Future<T> onFailure(Consumer<Throwable> fn) {
		respond(e -> e.ifRight(fn));
        return this;
	}

    default Future<T> mayRescue(UnsafeFunction<Throwable, Future<T>> fn) {
        return transform(e -> e.fold(Futures::success, Futures.safe(fn)));
    }

    default Future<T> rescue(UnsafeFunction<Throwable, T> fn) {
        return mayRescue(fn.eitherFunction().andThen(Future::constant));
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
		public void respond(Consumer<Either<T, Throwable>> fn) {
			fn.accept(e);
		}

        @Override
        public <TT> Future<TT> transform(Function<Either<T, Throwable>, Future<TT>> fn) {
            return fn.apply(e);
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
		public void respond(Consumer<Either<Object, Throwable>> fn) {

		}

        @Override
        public <TT> Future<TT> transform(Function<Either<Object, Throwable>, Future<TT>> fn) {
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
