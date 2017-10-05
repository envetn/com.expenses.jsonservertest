import java.io.Serializable;

/**
 * Created by olof on 2016-07-13.
 */
public class Request implements Serializable
{
    private String jsonRequest;

    public Request(String request)
    {
        jsonRequest = request;
    }
}
