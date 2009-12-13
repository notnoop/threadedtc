package mtc.sanity;


import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.notnoop.threadedtc.*;
import static com.notnoop.threadedtc.RunnerConductor.*;

@RunWith(TCRunner.class)
public class TestFrameworkTests
{

    int i = 0;

    @Threaded("1")
    public void thisWillRunInThread1()
    {
        i++;
    }

    @Threaded("2")
    public void thisWillRunInThread2()
    {
        waitForBeat(1);
        i++;
    }

    @Test
    @MultiThreadedTest(times = 3)
    public void testRunThreeTimes() throws Throwable
    {
        assertEquals(i, 6);
    }

}
