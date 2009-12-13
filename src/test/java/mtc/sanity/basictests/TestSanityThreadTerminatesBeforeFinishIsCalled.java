package mtc.sanity.basictests;

import com.notnoop.threadedtc.*;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
@RunWith(TCRunner.class)
public class TestSanityThreadTerminatesBeforeFinishIsCalled
{
    Thread t1, t2;

    @Threaded
    public void thread1()
    {
        t1 = Thread.currentThread();
    }

    @Threaded
    public void thread2()
    {
        t2 = Thread.currentThread();
    }

    @Test
    public void finish()
    {
        org.junit.Assert.assertEquals(Thread.State.TERMINATED, t1.getState());
        org.junit.Assert.assertEquals(Thread.State.TERMINATED, t2.getState());
    }
}
