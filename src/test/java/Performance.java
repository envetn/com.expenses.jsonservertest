import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import databaseUtils.DatabaseTestHelper;

import com.google.gson.JsonObject;

/**
 * Created by eolochr on 12/25/16.
 */
public class Performance extends BaseTest
{
    @Test
    public void twoRequests() throws SQLException
    {
        //remove_me3
        String expectedCost = "20";
        Map<String, String> data = new HashMap<>();
        data.put("cost", expectedCost);
        data.put("costType", "bill");
        data.put("buyDate", "2016-04-02");
        data.put("comment", "removeMe");

        DatabaseTestHelper database = new DatabaseTestHelper();
        for (int i = 0; i < 4; i++)
        {
            database.insertExpensesValues(data, "remove_me3");
            data.put("buyDate", "2016-04-03");
        }

        //remove_me2
         expectedCost = "50";
        data = new HashMap<>();
        data.put("cost", expectedCost);
        data.put("costType", "food");
        data.put("buyDate", "2015-04-02");
        data.put("comment", "removeMe");

        for (int i = 0; i < 4; i++)
        {
            database.insertExpensesValues(data, "remove_me2");
            data.put("buyDate", "2015-04-03");
        }

        JsonObject jsonRequest_remove2 = createGetRequest("Expenses", "buyDate");
        JsonObject userObject = createUserObject("remove_me2");
        jsonRequest_remove2.add("user", userObject);

        JsonObject jsonRequest_remove3 = createGetRequest("Expenses", "buyDate");
        JsonObject userObject_remove3 = createUserObject("remove_me3");
        jsonRequest_remove3.add("user", userObject_remove3);


        ExecutorService service = Executors.newFixedThreadPool(2);
        Runner request_remove_me2 = new Runner(jsonRequest_remove2);
        Runner request_remove_me3 = new Runner(jsonRequest_remove3);

        service.submit(request_remove_me2);
        System.out.println("Executing thread 1");
        service.submit(request_remove_me3);
        System.out.println("Executing thread 2");

//        for(int i=0; i<20; i++)
//        {
//            System.out.println("Executing thread 1");
//            request_remove_me2.run();
//
//            System.out.println("Executing thread 2");
//            request_remove_me3.run();
//
//        }
    }


    private class Runner implements Runnable
    {
        private final JsonObject myRequest;
        private String myResponse;

        public Runner(JsonObject request)
        {
            myRequest = request;
        }

        public String getResponse()
        {
            return myResponse;
        }

        @Override
        public void run()
        {
            myResponse = executeNetworkPost(myRequest);
        }
    }
}
