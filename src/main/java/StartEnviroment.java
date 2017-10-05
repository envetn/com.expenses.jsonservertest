import java.net.UnknownHostException;

/**
 * Created by lofie on 2017-09-17.
 */
public class StartEnviroment
{
    public static void main(String[] argv)
    {
        JsonServerHandler jsonServerHandler = new JsonServerHandler();

        try
        {
            jsonServerHandler.start();
        }
        catch (InterruptedException | UnknownHostException e)
        {
            e.printStackTrace();
        }

//        while (true)
//        {
//            // Keep looping
//        }

    }
}
