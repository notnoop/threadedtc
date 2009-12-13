package mtc.compare;

import com.notnoop.threadedtc.*;
import static com.notnoop.threadedtc.RunnerConductor.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.notnoop.threadedtc.TCRunner;

import static org.junit.Assert.*;

/**
 * In this test, the first offer is allowed to timeout, the second offer is interrupted. Use `freezeClock` to prevent the clock from
 * advancing during the first offer.
 */
@RunWith(TCRunner.class)
public class TimeoutMTCTest
{
    ArrayBlockingQueue<Object> q;

    @Before
    public void initialize()
    {
        q = new ArrayBlockingQueue<Object>(2);
    }

    @Threaded("1")
    public void thread1()
    {
        try
        {
            q.put(new Object());
            q.put(new Object());

//            freezeClock();
            assertFalse(q.offer(new Object(), 25, TimeUnit.MILLISECONDS));
//            unfreezeClock();

            q.offer(new Object(), 2500, TimeUnit.MILLISECONDS);
            fail("should throw exception");
        } catch (InterruptedException success)
        {
            assertEquals(1, beat());
        }
    }

    @Threaded
    public void thread2()
    {
        waitForBeat(1);
        getThread("1").interrupt();
    }

    @Test
    public void test()
    {
    }
}
