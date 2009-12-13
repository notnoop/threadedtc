package mtc.sanity.basictests;

import com.notnoop.threadedtc.*;

import static com.notnoop.threadedtc.RunnerConductor.*;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
@RunWith(TCRunner.class)
public class TestSanityWaitForTickBlocksThread
{
    Thread t;

    @Threaded
    public void thread1()
    {
        t = Thread.currentThread();
        waitForBeat(2);
    }

    @Threaded
    public void thread2()
    {
        waitForBeat(1);
        assertEquals(Thread.State.WAITING, t.getState());
    }

    @Test
    @Ignore("threads may have BLOCK state for some reason")
    public void test()
    {
    }
}
