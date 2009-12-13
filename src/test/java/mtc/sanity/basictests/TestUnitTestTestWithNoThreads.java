package mtc.sanity.basictests;

import com.notnoop.threadedtc.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.junit.After;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:jvb@newtec.eu">Jan Van Besien</a>
 */
@RunWith(TCRunner.class)
public class TestUnitTestTestWithNoThreads
{
    private AtomicInteger v1;

    @Test
    public void initialize()
    {
        v1 = new AtomicInteger(0);
        assertTrue(v1.compareAndSet(0, 1));
    }

    @After
    public void finish()
    {
        assertTrue(v1.compareAndSet(1, 2));
    }
}
