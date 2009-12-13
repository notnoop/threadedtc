package mtc.sanity.errordetectiontests;

import java.util.concurrent.locks.ReentrantLock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.notnoop.threadedtc.*;
import com.notnoop.threadedtc.exceptions.DeadlockSuspectedError;

import static com.notnoop.threadedtc.RunnerConductor.*;

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
@RunWith(TCRunner.class)
public class TestTUnitTestDeadlockDetected
{
    ReentrantLock lockA;
    ReentrantLock lockB;

    @Before
    public void initialize()
    {
        lockA = new ReentrantLock();
        lockB = new ReentrantLock();
    }

    @Threaded
    public void threadA()
    {
        lockA.lock();
        waitForBeat(1);
        lockB.lock();
    }

    @Threaded
    public void threadB()
    {
        lockB.lock();
        waitForBeat(1);
        lockA.lock();
    }

    @Test(expected = DeadlockSuspectedError.class)
    public void test()
    {
    }
}