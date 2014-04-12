package org.fungsi.concurrent;

import org.fungsi.Unit;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

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
}
