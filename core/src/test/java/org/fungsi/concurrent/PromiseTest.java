package org.fungsi.concurrent;

import org.fungsi.Either;
import org.fungsi.Unit;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.Executors;

import static org.fungsi.Matchers.isDone;
import static org.fungsi.Matchers.isSuccess;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PromiseTest {
	@Test
	public void testMap() throws Exception {
		Promise<String> p = Promises.create();
		Future<Integer> fut = p.map(String::length);

		p.set(Either.success("lol"));

		assertThat(fut.get(), is(3));
	}

	@Test
	public void testFilter() throws Exception {
		Promise<Unit> p = Promises.create();
		Future<Unit> fut = p.filter(it -> true);

		p.set(Unit.left());

		assertThat(fut, isDone());
	}

    @Test
    public void testIssue2() throws Exception {
        // given
        Promise<String> p = Promises.create();

        // when
        p.complete("foo");
        Future<Integer> f = p.map(String::length);

        // then
        assertThat(f, isSuccess());
        assertThat(f.get(), equalTo(3));
    }

	@Test
	public void testGet() throws Exception {
		Worker w = Workers.wrap(Executors.newCachedThreadPool());

		Future<Unit> f = w.cast(() -> Thread.sleep(1000));
		Future<Unit> ff = f.flatMap(u -> w.cast(() -> Thread.sleep(1000)));

		long start = System.currentTimeMillis();
		ff.get();
		long elapsed = System.currentTimeMillis() - start;

		assertTrue(2000 - elapsed < 50);
	}

	@Test
	public void testGetTimeoutSuccess() throws Exception {
		Worker w = Workers.wrap(Executors.newCachedThreadPool());

		Future<Unit> f = w.cast(() -> Thread.sleep(1000));
		Future<Unit> ff = f.flatMap(u -> w.cast(() -> Thread.sleep(1000)));

		long start = System.currentTimeMillis();
		ff.get(Duration.ofMillis(3000));
		long elapsed = System.currentTimeMillis() - start;

		assertTrue(2000 - elapsed < 50);
	}

	@Test(expected = TimeoutException.class)
	public void testGetTimeoutFailure() throws Exception {
		Worker w = Workers.wrap(Executors.newCachedThreadPool());

		Future<Unit> f = w.cast(() -> Thread.sleep(1000));
		Future<Unit> ff = f.flatMap(u -> w.cast(() -> Thread.sleep(1000)));

		ff.get(Duration.ofMillis(1000));
		fail();
	}
}
