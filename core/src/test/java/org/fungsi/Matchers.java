package org.fungsi;

import org.fungsi.concurrent.Future;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.time.Duration;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.not;

public final class Matchers {
	private Matchers() {}

	@SuppressWarnings("unchecked")
	public static <T> Matcher<Optional<T>> isPresent() {
		return (Matcher) IS_PRESENT;
	}

	@SuppressWarnings("unchecked")
	public static <L, R> Matcher<Either<L, R>> isLeft() {
		return (Matcher) IS_LEFT;
	}

	@SuppressWarnings("unchecked")
	public static <L, R> Matcher<Either<L, R>> isRight() {
		return (Matcher) IS_RIGHT;
	}

	public static <T> Matcher<Optional<T>> notPresent() {
		return not(isPresent());
	}

	public static Matcher<Duration> about(Duration d, Duration factor) {
		return new About(d, factor);
	}

	@SuppressWarnings("unchecked")
	public static <T> Matcher<Future<T>> isDone() {
		return (Matcher) IS_DONE;
	}

	@SuppressWarnings("unchecked")
	public static <T> Matcher<Future<T>> isSuccess() {
		return (Matcher) IS_SUCCESS;
	}

	@SuppressWarnings("unchecked")
	public static <T> Matcher<Future<T>> isFailure() {
		return (Matcher) IS_FAILURE;
	}

	private static final Matcher<Optional> IS_PRESENT = new BaseMatcher<Optional>() {
		@Override
		public boolean matches(Object item) {
			return ((Optional) item).isPresent();
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("is present");
		}
	};

	private static final Matcher<Either> IS_LEFT = new BaseMatcher<Either>() {
		@Override
		public boolean matches(Object item) {
			return ((Either) item).isLeft();
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("is left");
		}
	};

	private static final Matcher<Either> IS_RIGHT = new BaseMatcher<Either>() {
		@Override
		public boolean matches(Object item) {
			return ((Either) item).isRight();
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("is right");
		}
	};

	private static final class About extends BaseMatcher<Duration> {
		private final Duration d, factor;

		private About(Duration d, Duration factor) {
			this.d = d;
			this.factor = factor;
		}

		@Override
		public boolean matches(Object item) {
			Duration delta = ((Duration) item).minus(d);
			long ns = Math.abs(delta.toNanos());
			long f = Math.abs(factor.toNanos());
			return ns <= f;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("is about ");
			description.appendValue(d);
			description.appendText(" (factor ");
			description.appendValue(factor);
			description.appendText(")");
		}
	}

	private static final Matcher<Future> IS_DONE = new BaseMatcher<Future>() {
		@Override
		public boolean matches(Object item) {
			return ((Future) item).isDone();
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("is done");
		}
	};

	private static final Matcher<Future> IS_SUCCESS = new BaseMatcher<Future>() {
		@Override
		public boolean matches(Object item) {
			return ((Future) item).isSuccess();
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("is success");
		}
	};

	private static final Matcher<Future> IS_FAILURE = new BaseMatcher<Future>() {
		@Override
		public boolean matches(Object item) {
			return ((Future) item).isFailure();
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("is failure");
		}
	};
}
