package mtc.sanity.errordetectiontests;

import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.notnoop.threadedtc.*;
import com.notnoop.threadedtc.exceptions.TimeoutError;

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
@RunWith(TCRunner.class)
public class TestTUnitTestLiveLockTimesOut
{
    AtomicInteger ai;

    @Before
    public void initialize()
    {
        ai = new AtomicInteger(1);
    }

    @Threaded
    public void thread1()
    {
        while (!ai.compareAndSet(2, 3)) Thread.yield();
    }

    @Threaded
    public void thread2()
    {
        while (!ai.compareAndSet(3, 2)) Thread.yield();
    }

    @Test(expected = TimeoutError.class)
    public void finish()
    {
        assertTrue(ai.get() == 2 || ai.get() == 3);
    }
}
