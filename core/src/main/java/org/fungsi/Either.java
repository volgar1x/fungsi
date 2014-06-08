package org.fungsi;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public interface Either<L, R> {
	public static <L, R> Either<L, R> left(L left) { return new Left<>(left); }
	public static <L, R> Either<L, R> right(R right) { return new Right<>(right); }

	public static <T> Either<T, Throwable> success(T value) { return left(value); }
	public static <T> Either<T, Throwable> failure(Throwable cause) { return right(cause); }
	public static <T> T unsafe(Either<T, Throwable> e) {
        return e.fold(Function.identity(), r -> {
            throw Throwables.propagate(r);
        });
	}

    public static <T> Either<T, Unit> of(Optional<T> option) {
        return option.<Either<T, Unit>>map(Either::left).orElse(Unit.right());
    }

	L left();
	boolean isLeft();
	R right();
	boolean isRight();

	<LL, RR> Either<LL, RR> bind(Function<L, Either<LL, RR>> left, Function<R, Either<LL, RR>> right);

	<T> T fold(Function<L, T> left, Function<R, T> right);

	default <LL, RR> Either<LL, RR> map(Function<L, LL> left, Function<R, RR> right) {
		return bind(left.andThen(Either::left), right.andThen(Either::right));
	}

	default <LL> Either<LL, R> leftFlatMap(Function<L, Either<LL, R>> left) {
		return bind(left, Either::right);
	}

	default <LL> Either<LL, R> leftMap(Function<L, LL> left) {
		return leftFlatMap(left.andThen(Either::left));
	}

	default Either<L, R> leftFilter(Supplier<R> otherwise, Predicate<L> fn) {
		return leftFlatMap(l -> fn.test(l) ? left(l) : right(otherwise.get()));
	}

    default Optional<L> leftOption() {
        return this.<Optional<L>>fold(Optional::of, x -> Optional.empty());
    }

    default Either<L, R> leftFallback(Supplier<Either<L, R>> fn) {
        return bind(Either::left, r -> fn.get());
    }

    default L leftOrElse(Supplier<L> fn) {
        return fold(Function.identity(), r -> fn.get());
    }

    default L leftOrThrow(Supplier<Throwable> fn) {
        return fold(Function.identity(), r -> {
            throw Throwables.propagate(fn.get());
        });
    }

	default <RR> Either<L, RR> rightFlatMap(Function<R, Either<L, RR>> right) {
		return bind(Either::left, right);
	}

	default <RR> Either<L, RR> rightMap(Function<R, RR> right) {
		return rightFlatMap(right.andThen(Either::right));
	}

	default Either<L, R> rightFilter(Supplier<L> otherwise, Predicate<R> fn) {
		return rightFlatMap(r -> fn.test(r) ? right(r) : left(otherwise.get()));
	}

    default Optional<R> rightOption() {
        return this.<Optional<R>>fold(x -> Optional.empty(), Optional::of);
    }

    default Either<L, R> rightFallback(Supplier<Either<L, R>> fn) {
        return bind(l -> fn.get(), Either::right);
    }

    default R rightOrElse(Supplier<R> fn) {
        return fold(l -> fn.get(), Function.identity());
    }

    default R rightOrThrow(Supplier<Throwable> fn) {
        return fold(l -> {
            throw Throwables.propagate(fn.get());
        }, Function.identity());
    }

	default Either<L, R> ifLeft(Consumer<L> fn) {
		return leftFlatMap(l -> {
			fn.accept(l);
			return Either.this;
		});
	}

	default Either<L, R> ifRight(Consumer<R> fn) {
		return rightFlatMap(r -> {
			fn.accept(r);
			return Either.this;
		});
	}

	default Either<R, L> swap() {
		return bind(Either::right, Either::left);
	}

	default Matcher<L, R> match(Consumer<L> fn) {
		return new Matcher<>(this, fn);
	}

	default <T> FoldToRight<R, T> foldLeft(Function<L, T> fn) {
		return new Folder<L, R, T>(this).setLeft(fn);
	}

	default <T> FoldToLeft<L, T> foldRight(Function<R, T> fn) {
		return new Folder<L, R, T>(this).setRight(fn);
	}

	static final class Left<L, R> implements Either<L, R> {

		final L value;

		Left(L value) {
			this.value = value;
		}

		@Override
		public L left() {
			return value;
		}

		@Override
		public boolean isLeft() {
			return true;
		}

		@Override
		public R right() {
			throw new IllegalStateException();
		}

		@Override
		public boolean isRight() {
			return false;
		}

		@Override
		public <LL, RR> Either<LL, RR> bind(Function<L, Either<LL, RR>> left, Function<R, Either<LL, RR>> right) {
			return left.apply(value);
		}

		@Override
		public <T> T fold(Function<L, T> left, Function<R, T> right) {
			return left.apply(value);
		}
	}

	static final class Right<L, R> implements Either<L, R> {

		final R value;

		Right(R value) {
			this.value = value;
		}

		@Override
		public L left() {
			throw new IllegalStateException();
		}

		@Override
		public boolean isLeft() {
			return false;
		}

		@Override
		public R right() {
			return value;
		}

		@Override
		public boolean isRight() {
			return true;
		}

		@Override
		public <LL, RR> Either<LL, RR> bind(Function<L, Either<LL, RR>> left, Function<R, Either<LL, RR>> right) {
			return right.apply(value);
		}

		@Override
		public <T> T fold(Function<L, T> left, Function<R, T> right) {
			return right.apply(value);
		}
	}

	@SuppressWarnings("UnusedParameters")
	public static <L, R> Either<L, R> left(L left, Class<R> ignored) { return left(left); }
	@SuppressWarnings("UnusedParameters")
	public static <L, R> Either<L, R> right(Class<L> ignored, R right) { return right(right); }

	public static final class Matcher<L, R> {
		private final Either<L, R> value;
		private final Consumer<L> left;

		Matcher(Either<L, R> value, Consumer<L> left) {
			this.value = value;
			this.left = left;
		}

		public Either<L, R> then(Consumer<R> right) {
			value.ifLeft(left);
			value.ifRight(right);
			return value;
		}
	}

	public static interface FoldToLeft<L, T> {
		T thenLeft(Function<L, T> fn);
	}

	public static interface FoldToRight<R, T> {
		T thenRight(Function<R, T> fn);
	}

	public static final class Folder<L, R, T> implements FoldToLeft<L, T>, FoldToRight<R, T> {
		private final Either<L, R> value;
		private Function<L, T> left;
		private Function<R, T> right;

		Folder(Either<L, R> value) {
			this.value = value;
		}

		Folder<L, R, T> setLeft(Function<L, T> left) {
			this.left = left;
			return this;
		}

		Folder<L, R, T> setRight(Function<R, T> right) {
			this.right = right;
			return this;
		}

		T proceed() {
			return value.fold(requireNonNull(left, "left"), requireNonNull(right, "right"));
		}

		@Override
		public T thenLeft(Function<L, T> fn) {
			return setLeft(fn).proceed();
		}

		@Override
		public T thenRight(Function<R, T> fn) {
			return setRight(fn).proceed();
		}
	}
}
