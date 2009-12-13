package mtc.sanity.basictests;

import com.notnoop.threadedtc.*;

import static com.notnoop.threadedtc.RunnerConductor.*;

import static org.junit.Assert.assertSame;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
@RunWith(TCRunner.class)
public class TestSanityGetThreadReturnsCorrectThread
{
    Thread t;

    @Threaded("1")
    public void thread1()
    {
        t = Thread.currentThread();
        waitForBeat(2);
    }

    @Threaded("2")
    public void thread2()
    {
        waitForBeat(1);
        assertSame(getThread("1"), t);
    }

    @Test
    public void test()
    {
    }
}
