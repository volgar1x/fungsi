package org.fungsi.function;

import org.fungsi.Unit;
import org.fungsi.concurrent.Future;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class FutureTest {
	@Test(expected = IllegalStateException.class)
	public void testFilter() throws Exception {
		Future<Unit> fut = Future.unit();

		fut.filter(it -> false).get();
	}

	@Test
	public void testMap() throws Exception {
		Future<String> fut = Future.success("lol");

		int res = fut.map(String::length).get();

		assertThat(res, is(3));
	}
}
