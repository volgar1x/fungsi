package org.fungsi.concurrent;

import org.fungsi.Either;
import org.fungsi.Unit;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;

import static org.fungsi.Matchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ConcurrentTest {
	private Worker worker;
	private Timer timer;

	@Before
	public void setUp() throws Exception {
		worker = Workers.wrap(Executors.newSingleThreadExecutor());
		timer = Timers.wrap(Executors.newSingleThreadScheduledExecutor());
	}

	@Test
	public void testGet() throws Exception {
		Future<Unit> fut = worker.cast(() -> Thread.sleep(500));

		Instant start = Instant.now();
		fut.get();
		Instant end = Instant.now();

		assertThat(Duration.between(start, end), about(Duration.ofMillis(500), Duration.ofMillis(10)));
	}

	@Test
	public void testGetWithTimeout() throws Exception {
		Future<Unit> fut = worker.cast(() -> Thread.sleep(500));

		Instant start = Instant.now();
		fut.get(Duration.ofMillis(600));
		Instant end = Instant.now();

		assertThat(Duration.between(start, end), about(Duration.ofMillis(500), Duration.ofMillis(10)));
	}

	@Test(expected = TimeoutException.class)
	public void testFailingGetWithTimeout() throws Exception {
		Future<Unit> fut = worker.cast(() -> Thread.sleep(500));

		fut.get(Duration.ofMillis(100));
	}

	@Test
	public void testSuccess() throws Exception {
		Future<String> fut = worker.submit(() -> {
			Thread.sleep(500);
			return "mdr";
		});

		assertThat(fut.poll(), notPresent());

		Thread.sleep(1000);
		assertThat(fut.poll(), isPresent());

		Either<String, Throwable> result = fut.poll().get();
		assertThat(result, isLeft());
		assertThat(result.left(), is("mdr"));
	}

	@Test(expected = NullPointerException.class)
	public void testFailure() throws Exception {
		Future<String> fut = worker.submit(() -> {
			Thread.sleep(500);
			throw new NullPointerException();
		});

		assertThat(fut.poll(), notPresent());

		Thread.sleep(1000);
		assertThat(fut.poll(), isPresent());

		Either<String, Throwable> result = fut.poll().get();
		assertThat(result, isRight());
		Either.unsafe(result);
	}

	@Test
	public void testWithin() throws Exception {
		Future<Unit> fut = worker.cast(() -> Thread.sleep(750));
		Future<Unit> fut2 = fut.within(Duration.ofMillis(500), timer);

		Thread.sleep(1000);

		assertThat(fut, isSuccess());
		assertThat(fut2, isFailure());
	}
}
