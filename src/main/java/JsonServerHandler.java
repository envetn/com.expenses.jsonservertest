import org.apache.log4j.Logger;
import server.ServerRunner;

import com.expenses.spark.api.StartSpark;

import java.net.UnknownHostException;

/**
 * Created by olof on 2016-08-27.
 */
public class JsonServerHandler
{
    private final static Logger LOG = Logger.getLogger(JsonServerHandler.class);
    private final ServerRunner myRunner;
    private Runnable sparkTask;
    private Runnable jsonTask;

    public JsonServerHandler()
    {
        StartSpark sparkHandler = new StartSpark();
        sparkTask = sparkHandler::fireUp;

        myRunner = new ServerRunner(false, true, 9875);
    }

    public void start() throws InterruptedException, UnknownHostException
    {
        Thread thread = new Thread(sparkTask);
        thread.start();

        waitForService(0);
        myRunner.startServer();

        LOG.info("Service registered and server stared. waiting..");
        Thread.sleep(2000L);
    }

    private void waitForService(int times) throws UnknownHostException, InterruptedException
    {
        boolean b = myRunner.registerService();
        if (! b && times < 3)
        {
            LOG.info("Service not registered, retrying..");
            Thread.sleep(2000L);
            times += 1;
            waitForService(times);
        }
    }

    public void tearDown()
    {
//        myRunner.tearDown();
//        Thread.currentThread().interrupt();
    }
}
