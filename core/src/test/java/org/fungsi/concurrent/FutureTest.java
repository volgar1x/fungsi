package org.fungsi.concurrent;

import org.fungsi.Unit;
import org.fungsi.function.UnsafeSupplier;
import org.junit.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import static org.fungsi.Matchers.isFailure;
import static org.fungsi.Matchers.isSuccess;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class FutureTest {
	@Test(expected = IllegalStateException.class)
	public void testFilter() throws Exception {
		Future<Unit> fut = Futures.unit();

		fut.filter(it -> false).get();
	}

	@Test
	public void testMap() throws Exception {
		Future<String> fut = Futures.success("lol");

		int res = fut.map(String::length).get();

		assertThat(res, is(3));
	}

	@Test
	public void testCollect() throws Exception {
		Future<List<String>> fut = Futures.collect(Arrays.asList(
				Futures.success("lol"),
				Futures.success("mdr"),
				Futures.success("lmao")
		));

		assertThat(fut, isSuccess());
		assertThat(fut.get(), hasItems("lol", "mdr", "lmao"));
	}

    @Test
    public void testCollect_threaded() throws Exception {
        Worker worker = Workers.wrap(Executors.newFixedThreadPool(3));

        Future<List<String>> fut = Futures.collect(Arrays.asList(
                worker.submit(sleepThenReturn("lol", 1000L)),
                worker.submit(sleepThenReturn("mdr", 1000L)),
                worker.submit(sleepThenReturn("lmao", 1000L))
        ));

        assertThat(fut.get(Duration.ofMillis(1100L)), hasItems("lol", "mdr", "lmao"));
        assertThat(fut, isSuccess());
    }

    private static <T> UnsafeSupplier<T> sleepThenReturn(T val, long millis) {
        return () -> { Thread.sleep(millis); return val; };
    }

    @Test
    public void testCollect_withFailure() throws Exception {
        Future<List<String>> fut = Futures.collect(Arrays.asList(
                Futures.<String>failure(new Exception()),
                Futures.success("mdr"),
                Futures.success("lmao")
        ));

        assertThat(fut, isFailure());
    }
}
