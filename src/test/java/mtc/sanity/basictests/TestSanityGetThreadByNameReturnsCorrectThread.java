package mtc.sanity.basictests;

import com.notnoop.threadedtc.*;
import static com.notnoop.threadedtc.RunnerConductor.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
@RunWith(TCRunner.class)
public class TestSanityGetThreadByNameReturnsCorrectThread
{
    Thread t;

    @Threaded("Fooey")
    public void threadFooey()
    {
        t = Thread.currentThread();

        waitForBeat(2);
    }

    @Threaded
    public void threadBooey()
    {
        waitForBeat(1);
        assertEquals("threadBooey", Thread.currentThread().getName());
        assertSame(getThread("Fooey"), t);
    }

    @Test
    public void test()
    {
        assertEquals(t.getName(), "Fooey");
    }

}
