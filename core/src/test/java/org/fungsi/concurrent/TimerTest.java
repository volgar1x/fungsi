package org.fungsi.concurrent;

import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.Executors;

import static org.fungsi.Matchers.isDone;
import static org.fungsi.Matchers.notDone;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TimerTest {
	private Timer timer;

	@Before
	public void setUp() throws Exception {
		timer = Timers.wrap(Executors.newSingleThreadScheduledExecutor());
	}

	@Test
	public void testSchedule() throws Exception {
		Future<String> fut = timer.schedule(Duration.ofMillis(100), () -> "lol");

		assertThat(fut, notDone());

		Thread.sleep(105);

		assertThat(fut, isDone());
		assertThat(fut.get(), is("lol"));
	}
}
