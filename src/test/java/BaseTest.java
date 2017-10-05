import static java.util.Arrays.asList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.assertj.core.api.Java6Assertions.within;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.*;

import databaseUtils.DatabaseTestHelper;

import com.google.gson.JsonObject;

/**
 * Created by olof on 2016-07-13.
 */
public abstract class BaseTest
{
    private static final Logger LOGGER = Logger.getLogger(BaseTest.class);
    protected Socket mySocket;
    protected BufferedReader bufferReaderIn;
    protected PrintWriter printWriterOut;
    protected final int PORT = 8080;
    protected final String IP_ADDR = "127.0.0.1";

    protected static final DatabaseTestHelper DATABSAE_HELPER = new DatabaseTestHelper();
    private static final JsonServerHandler handler = new JsonServerHandler();
    private static AtomicBoolean hasStarted = new AtomicBoolean(false);
    private static AtomicInteger numberOfTests = new AtomicInteger(3);

    @BeforeClass
    public static void preInit() throws InterruptedException, UnknownHostException
    {
        DATABSAE_HELPER.addUser("remove_me1");
        DATABSAE_HELPER.addUser("remove_me2");
        DATABSAE_HELPER.addUser("remove_me3");
        DATABSAE_HELPER.addUser("remove_me4");

        if(!hasStarted.get())
        {
            hasStarted.set(true);
            LOGGER.info("Starting servers...");
            System.out.println("Starting servers..");
            handler.start();
        }
    }

    @AfterClass
    public static void preAfter()
    {
        DATABSAE_HELPER.clearUsernameAndData("remove_me1");
        DATABSAE_HELPER.clearUsernameAndData("remove_me2");
        DATABSAE_HELPER.clearUsernameAndData("remove_me3");
        DATABSAE_HELPER.clearUsernameAndData("remove_me4");
        int i = numberOfTests.decrementAndGet();
        if(i <= 0)
        {
            LOGGER.info("Stopping servers...");
            handler.tearDown();
        }

    }

    @Before
    public void init() throws IOException
    {
    }

    @After
    public void after()
    {
    }

    protected String executeNetworkPost(JsonObject content)
    {
        String request = content.toString() + System.getProperty("line.separator");
        String response;

        try
        {
            HttpURLConnection urlConnection = initUrlConnection();

//            OutputStreamWriter outputStrem = new OutputStreamWriter(urlConnection.getOutputStream());
            OutputStream output = urlConnection.getOutputStream();
            output.write(request.getBytes());
            output.flush();

            urlConnection.connect();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));

            response = bufferedReader.readLine();

            urlConnection.disconnect();

            bufferedReader.close();
            output.close();
        }
        catch (IOException e)
        {
            response = "Exception caught: " + e.getMessage();
        }

        validateExecutionTime(response);

        return response;
    }

    private void validateExecutionTime(String response)
    {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(response)
                .getAsJsonObject();
        long executionTime = jsonObject.get("executionTime")
                .getAsLong();

//        SoftAssert softAssert = new SoftAssert();
//        assertThat(executionTime)
//                .as("The request time should not be longer than 50Milli")
//                .isCloseTo(5L, within(50L)); // between 5 and 55 milli
    }

    private HttpURLConnection initUrlConnection()
    {
//        "http://192.168.1.10:8080/RESTexample/rest/register/json
//        String address = String.format("http://%s:%d/RESTexample/rest/expenses/json", IP_ADDR, PORT);

        try
        {
            String address = "http://"+IP_ADDR+":4567/expenses";

            URL url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/jsonserver.common.json");
            connection.setReadTimeout(10000 /* milliseconds */);
            connection.setConnectTimeout(15000 /* milliseconds */);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            return connection;
        }
        catch (IOException e)
        {
            System.out.println("Error" + e.toString());
            throw new IllegalStateException(e);
        }
    }


    public static void removeSingleUser(String user)
    {
        DATABSAE_HELPER.clearUsernameAndData("Expenses_" + user);
        DATABSAE_HELPER.removeUser(Collections.singletonList(user));
    }

    protected JsonObject createJsonHeader(String requestType, Date time, String requestId)
    {
        JsonObject jsonObject = new JsonObject();
        JsonObject requestObject = new JsonObject();
        requestObject.addProperty("requestId", requestId);

        jsonObject.add("id", requestObject);
        jsonObject.addProperty("requestType", requestType);
        jsonObject.addProperty("requestDate", time.toString());

        return jsonObject;
    }

    protected JsonObject createJsonHeader(String requestType, DateTime time, String requestId)
    {
        JsonObject jsonObject = new JsonObject();
        JsonObject requestObject = new JsonObject();
        requestObject.addProperty("requestId", requestId);

        jsonObject.add("id", requestObject);
        jsonObject.addProperty("requestType", requestType);
        jsonObject.addProperty("requestDate", time.toString());

        return jsonObject;
    }


    protected JsonObject createGetRequest(String type, String orderby)
    {
        return createGetRequest(type, orderby, "All", "");
    }

    protected JsonObject createGetRequest(String type, String orderBy, String period, String periodToFetch)
    {
        Date timestamp = new Date(Calendar.getInstance()
                .getTimeInMillis());
        JsonObject jsonRequest = createJsonHeader("Get", timestamp, type);

        JsonObject orderObject = new JsonObject();
        orderObject.addProperty("orderBy", orderBy);
        orderObject.addProperty("isAscending", true);

        JsonObject limitObject = new JsonObject();
        JsonObject fetchperiodObject = new JsonObject();
        fetchperiodObject.addProperty("period", period);
        fetchperiodObject.addProperty("periodToFetch", periodToFetch);
        fetchperiodObject.addProperty("hasPeriod", true);

        limitObject.add("fetchperiod", fetchperiodObject);

        jsonRequest.add("order", orderObject);
        jsonRequest.add("limit", limitObject);

        return jsonRequest;
    }

    //TODO: clean up fetchPeriod
    protected JsonObject createGetRequestNew(String type, String orderBy, String period, String periodToFetch, Map<String, LocalDate> minMax)
    {
        Date timestamp = new Date(Calendar.getInstance()
                .getTimeInMillis());
        JsonObject jsonRequest = createJsonHeader("Get", timestamp, type);

        JsonObject orderObject = new JsonObject();
        orderObject.addProperty("orderBy", orderBy);
        orderObject.addProperty("isAscending", true);

        JsonObject limitObject = new JsonObject();
        JsonObject fetchperiodObject = new JsonObject();
        fetchperiodObject.addProperty("period", period);
        fetchperiodObject.addProperty("periodToFetch", periodToFetch);
        fetchperiodObject.addProperty("hasPeriod", true);
        fetchperiodObject.addProperty("timeStart", minMax.get("first")
                .toString());
        fetchperiodObject.addProperty("timeEnd", minMax.get("last")
                .toString());

        limitObject.add("fetchperiod", fetchperiodObject);

        jsonRequest.add("order", orderObject);
        jsonRequest.add("limit", limitObject);

        return jsonRequest;
    }


    protected static JsonObject createUserObject(String userName)
    {
        JsonObject userObject = new JsonObject();
        userObject.addProperty("userId", userName);
        userObject.addProperty("username", userName);
        userObject.addProperty("password", userName);
        return userObject;
    }
}
