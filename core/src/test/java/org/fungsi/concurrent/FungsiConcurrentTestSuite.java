package org.fungsi.concurrent;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ConcurrentTest.class,
        FutureTest.class,
        PromiseTest.class,
        TimerTest.class
})
public class FungsiConcurrentTestSuite {
}
