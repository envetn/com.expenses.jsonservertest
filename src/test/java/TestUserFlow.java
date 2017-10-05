import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import databaseUtils.DateUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * Created by Foten on 12/1/2016.
 */
public class TestUserFlow extends BaseTest
{

    private static final String BILL = "bill";
    private static final String A_COMMENT = "A comment";
    private static final String ID_TO_BE_REMOVED = "idToBeRemoved";
    private static final String EXPECTED_COST_200 = "200";

    private static final String USER_4 = "remove_me4";
    private static final String USER_1 = "remove_me1";


    @BeforeClass
    public static void setupTestData() throws Exception
    {
        //remove_me4
        Map<String, String> data = new HashMap<>();
        data.put("cost", EXPECTED_COST_200);
        data.put("costType", BILL);
        data.put("buyDate", "2016-05-20");
        data.put("comment", A_COMMENT);

        for (int i = 0; i < 4; i++)
        {
            data.put("uuid", "IdToBeRemoved" + USER_4 + "_" + i);
            DATABSAE_HELPER.insertExpensesValues(data, USER_4);
        }
        DATABSAE_HELPER.insertThreshold(200, 51 * 4, USER_4, BILL, 5);
    }

    @Test
    public void testCreateUser()
    {
        Date timestamp = new Date(Calendar.getInstance().getTimeInMillis());

        JsonObject jsonRequest = createJsonHeader("Put", timestamp, "User");
        jsonRequest.add("user", createUserObject("createdUser"));
        String response = executeNetworkPost(jsonRequest);
        validateResponse(response, "Success");
        removeSingleUser("createdUser");
    }

    @Test
    public void testGetUser()
    {
        JsonObject jsonRequest = createGetRequestNew("User", "", "timePeriod", "All", DateUtils.getBoTEoT());

        jsonRequest.add("user", createUserObject("remove_me4"));
        String response = executeNetworkPost(jsonRequest);
        validateResponse(response, "Success");

        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(response).getAsJsonObject();

        assertThat(jsonObject.get("Get-Data")).isNotNull();
        JsonObject getData = jsonObject.get("Get-Data").getAsJsonObject();

        assertThat(getData.get("user")).isNotNull();
        assertThat(getData.get("expenses")).isNotNull();
        assertThat(getData.get("temperature")).isNotNull();
    }

    @Test
    public void testCreateUserTwice()
    {
        Date timestamp = new Date(Calendar.getInstance().getTimeInMillis());

        JsonObject jsonRequest = createJsonHeader("Put", timestamp, "User");
        jsonRequest.add("user", createUserObject("createdUser"));
        String response = executeNetworkPost(jsonRequest);
        validateResponse(response, "Success");

        response = executeNetworkPost(jsonRequest);
        validateResponse(response, "Failed");

        removeSingleUser("createdUser");
    }

    private void validateResponse(String response, String responseCode)
    {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(response).getAsJsonObject();

        assertThat(jsonObject.get("Response").getAsString()).isEqualTo(responseCode);
        assertThat(jsonObject.get("Time").getAsString()).isNotNull();
    }
}
