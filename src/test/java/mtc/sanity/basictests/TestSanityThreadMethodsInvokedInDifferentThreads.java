package mtc.sanity.basictests;

import com.notnoop.threadedtc.*;

import static com.notnoop.threadedtc.RunnerConductor.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotSame;

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
@RunWith(TCRunner.class)
public class TestSanityThreadMethodsInvokedInDifferentThreads
{
    Thread t1, t2;

    @Threaded
    public void thread1()
    {
        t1 = Thread.currentThread();
        waitForBeat(2);
    }

    @Threaded
    public void thread2()
    {
        t2 = Thread.currentThread();
        waitForBeat(2);
    }

    @Threaded
    public void thread3()
    {
        waitForBeat(1);
        assertNotSame(t1, t2);
    }

    @Test
    public void test()
    {
    }
}
