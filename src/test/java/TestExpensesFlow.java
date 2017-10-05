
import com.google.gson.*;
import databaseUtils.DatabaseTestHelper;
import databaseUtils.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * Created by olof on 2016-07-12.
 */
public class TestExpensesFlow extends BaseTest
{

    private static final String BILL = "bill";
    private static final String A_COMMENT = "A comment";
    private static final String ID_TO_BE_REMOVED = "idToBeRemoved";
    private static final String EXPECTED_COST_200 = "200";

    private static final String USER_4 = "remove_me4";
    private static final String USER_2 = "remove_me2";
    private static final String USER_1 = "remove_me1";
    public static final JsonObject USER_REMOVE_ME_4 = createUserObject(USER_4);
    public static final JsonObject USER_REMOVE_ME_3 = createUserObject("remove_me3");

    private static final JsonObject USER_REMOVE_ME_2 = createUserObject(USER_2);

    private static final JsonObject USER_REMOVE_ME_1 = createUserObject(USER_1);
    private static final String EXPECTED_COST_20 = "20";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @BeforeClass
    public static void initTestData() throws SQLException
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

        LocalDateTime now = LocalDateTime
                .now()
                .withYear(2017)
                .withMonth(1)
                .withDayOfMonth(1);

        //remove_me3
        data = new HashMap<>();
        data.put("cost", EXPECTED_COST_20);
        data.put("costType", BILL);
        data.put("buyDate", now.format(FORMATTER));
        data.put("comment", A_COMMENT);

        for (int i = 0; i < 8; i++)
        {
            data.put("uuid", "IdToBeRemoved" + USER_2 + "_" + i);
            DATABSAE_HELPER.insertExpensesValues(data, USER_2);
            now = now.plusSeconds(864_000L);
            data.put("buyDate", now.format(FORMATTER));
        }

        //remove_me1
        data = new HashMap<>();
        data.put("cost", EXPECTED_COST_20);
        data.put("costType", BILL);
        data.put("buyDate", now.format(FORMATTER));
        data.put("comment", A_COMMENT);

        for (int i = 0; i < 4; i++)
        {
            data.put("uuid", "IdToBeRemoved" + USER_1 + "_" + i);
            DATABSAE_HELPER.insertExpensesValues(data, USER_1);
            now = now.plusSeconds(864_000L);
            data.put("buyDate", now.format(FORMATTER));
        }
    }

    //TODO stress test server
    @Test
    public void testCreateExpense()
    {
        Date timestamp = new Date(Calendar.getInstance()
                .getTimeInMillis());
        DateTime now = DateTime.now(DateTimeZone.UTC);

        JsonObject jsonRequest = createJsonHeader("Put", now, "Expenses");

        JsonObject contentObject = new JsonObject();
        contentObject.addProperty("cost", EXPECTED_COST_200);
        contentObject.addProperty("costType", BILL);
        contentObject.addProperty("comment", A_COMMENT);
        contentObject.addProperty("buyDate", now.toString());
        contentObject.addProperty("uuid", ID_TO_BE_REMOVED);

        jsonRequest.add("user", USER_REMOVE_ME_2);
        jsonRequest.add("content", contentObject);

        String response = executeNetworkPost(jsonRequest);
        validatePutResponse(response);
    }

    /**
     * Test Get request towards javaJsonServer
     */
    @Test
    public void testGetExpenseData() throws SQLException
    {
//        //remove_me4
        JsonObject jsonRequest = createGetRequestNew("Expenses", "buyDate", "timePeriod", "All", DateUtils.getBoTEoT());

        JsonObject userObject = createUserObject("remove_me4");

        jsonRequest.add("user", userObject);

        String response = executeNetworkPost(jsonRequest);
        validateGetResponse(response, EXPECTED_COST_200, 4);
    }

    /**
     * Test Get request towards javaJsonServer
     */
    @Test
    public void testGetExpenseDataMonthly() throws SQLException
    {
        //remove_me3
        Map<String, LocalDate> minMax = DateUtils.getFirstAndLastDayOf(1, 2017);

        JsonObject jsonRequest = createGetRequestNew("Expenses", "buyDate", "timePeriod", "10", minMax);

        JsonObject userObject = createUserObject("remove_me2");

        jsonRequest.add("user", userObject);

        String response = executeNetworkPost(jsonRequest);
        validateGetResponse(response, EXPECTED_COST_20, 4);
    }

    /**
     * Test Get request towards javaJsonServer
     */
    @Test
    public void testGetExpenseDataWeekly() throws SQLException
    {
//        //remove_me3
        Map<String, LocalDate> minMax = DateUtils.getFirstAndLastDayOfWeek();

        JsonObject jsonRequest = createGetRequestNew("Expenses", "buyDate", "timePeriod", "12", minMax);

        JsonObject userObject = createUserObject("remove_me2");

        jsonRequest.add("user", userObject);

        String response = executeNetworkPost(jsonRequest);
        validateGetResponse(response, EXPECTED_COST_20, 1);
    }

    /**
     * Test Get request towards javaJsonServer
     */
    @Test
    public void testGetExpenseDataDaily() throws SQLException
    {
        //remove_me3
        JsonObject jsonRequest = createGetRequestNew("Expenses", "buyDate", "timePeriod", "12", DateUtils.getSingleDay());

        JsonObject userObject = createUserObject("remove_me2");

        jsonRequest.add("user", userObject);

        String response = executeNetworkPost(jsonRequest);
        validateGetResponse(response, EXPECTED_COST_20, 1);
    }

    /**
     * Test send Remove request towards server
     */
    @Test
    public void testRemoveExpenseData() throws SQLException
    {
        Date timestamp = new Date(Calendar.getInstance().getTimeInMillis());

        JsonObject removeRequest = createJsonHeader("Delete", timestamp, "Expenses");

        JsonPrimitive element = new JsonPrimitive("IdToBeRemoved" + USER_1 + "_" + 0);
        JsonPrimitive element2 = new JsonPrimitive("IdToBeRemoved" + USER_1 + "_" + 1);
        JsonPrimitive element3 = new JsonPrimitive("IdToBeRemoved" + USER_1 + "_" + 2);
        JsonPrimitive element4 = new JsonPrimitive("IdToBeRemoved" + USER_1 + "_" + 3);

        JsonArray jsonArray = new JsonArray();
        jsonArray.add(element);
        jsonArray.add(element2);
        jsonArray.add(element3);
        jsonArray.add(element4);

        removeRequest.add("remove-Data", jsonArray);
        removeRequest.add("user", USER_REMOVE_ME_1);

        String response = executeNetworkPost(removeRequest);
        validateRemoveResponse(response);

    }

    /**
     * Test send invalid request type
     * <p>
     * request should fail with message "Failed"
     */
    @Test
    public void testSendInvalidGetRequest()
    {
        Date timestamp = new Date(Calendar.getInstance().getTimeInMillis());

        JsonObject jsonRequest = createJsonHeader("InvalidGet", timestamp, "Expenses");
        jsonRequest.addProperty("lowerLimit", 2);
        jsonRequest.addProperty("upperLimit", 4);
        jsonRequest.addProperty("orderBy", "buyDate DESC");

        jsonRequest.add("user", createUserObject("remove_me2"));

        String response = executeNetworkPost(jsonRequest);
        validateFailedResponse(response);

    }

    /**
     * Test send invalid requestId
     * <p>
     * request should fail with message "Failed"
     */
    @Test
    public void testSendInvalidRequestId()
    {
        Date timestamp = new Date(Calendar.getInstance().getTimeInMillis());

        JsonObject jsonRequest = createJsonHeader("Get", timestamp, "Invalid");
        jsonRequest.addProperty("lowerLimit", 2);
        jsonRequest.addProperty("upperLimit", 4);
        jsonRequest.addProperty("orderBy", "buyDate DESC");
        jsonRequest.add("user", createUserObject("remove_me2"));
        String response = executeNetworkPost(jsonRequest);
        validateFailedResponse(response);

    }

    /**
     * Test send Get request without user
     * <p>
     * request should fail with message "Failed"
     */
    @Test
    public void testSendGetWithoutUser()
    {
        JsonObject jsonRequest = createGetRequest("Expenses", "buyDate");
        String response = executeNetworkPost(jsonRequest);
        validateFailedResponse(response);
    }

    /**
     * Test send a unfinished getRequest without important parameters
     * <p>
     * request should fail with message "Failed"
     */
    @Test
    public void testSendUnfinishedGetRequest()
    {
        Date timestamp = new Date(Calendar.getInstance().getTimeInMillis());
        JsonObject jsonRequest = createJsonHeader("Get", timestamp, "Expenses");
        jsonRequest.add("user", createUserObject("remove_me"));
        String response = executeNetworkPost(jsonRequest);

        validateFailedResponse(response);
    }

    /**
     * Setup: Two users with different content saved.
     * <p>
     * The GetData Response should be different
     */
    @Test
    public void testGetRequestWithDifferentUser()
    {
        //First request json
        Date timestamp = new Date(Calendar.getInstance().getTimeInMillis());
        JsonObject firstJsonRequest = createJsonHeader("Put", timestamp, "Expenses");
        JsonObject contentObject = new JsonObject();
        contentObject.addProperty("cost", "200");
        contentObject.addProperty("costType", "bill");
        contentObject.addProperty("comment", "A comment");
        contentObject.addProperty("buyDate", timestamp.toString());
        contentObject.addProperty("uuid", "idToBeRemoved");

        JsonObject firstUserObject = createUserObject("remove_me2");

        firstJsonRequest.add("user", firstUserObject);
        firstJsonRequest.add("content", contentObject);

        //Second request json
        Date secondTimestamp = new Date(Calendar.getInstance().getTimeInMillis());
        JsonObject secondJsonRequest = createJsonHeader("Put", timestamp, "Expenses");
        JsonObject secondContentObject = new JsonObject();
        secondContentObject.addProperty("cost", "100");
        secondContentObject.addProperty("costType", "travel");
        secondContentObject.addProperty("comment", "A comment");
        secondContentObject.addProperty("buyDate", secondTimestamp.toString());
        secondContentObject.addProperty("uuid", "idToBeRemoved2");

        JsonObject secondUserObject = createUserObject("remove_me3");

        secondJsonRequest.add("user", secondUserObject);
        secondJsonRequest.add("content", secondContentObject);


        //Fire first and second request and then validate
        String firstResponse = executeNetworkPost(firstJsonRequest);
        String secondResponse = executeNetworkPost(secondJsonRequest);
        validatePutResponse(firstResponse);
        validatePutResponse(secondResponse);

        //////

        //First Get for user remove_me2
        JsonObject firstJsonRequestGet = createGetRequestNew("Expenses", "buyDate", "timePeriod", "All", DateUtils.getBoTEoT());
        firstJsonRequestGet.add("user", firstUserObject);

        JsonObject secondJsonRequestGet = createGetRequestNew("Expenses", "buyDate", "timePeriod", "All", DateUtils.getBoTEoT());
        secondJsonRequestGet.add("user", secondUserObject);

        // fire away the get requests
        firstResponse = executeNetworkPost(firstJsonRequestGet);
        secondResponse = executeNetworkPost(secondJsonRequestGet);

        JsonParser parser = new JsonParser();
        JsonObject firstJson = parser.parse(firstResponse).getAsJsonObject();
        JsonObject secondJson = parser.parse(secondResponse).getAsJsonObject();

        JsonObject firstJsonAsJsonArray = firstJson.getAsJsonArray("Get-Data").get(0).getAsJsonObject();
        JsonObject secondJsonAsJsonArray = secondJson.getAsJsonArray("Get-Data").get(0).getAsJsonObject();


        assertThat(firstJsonAsJsonArray.get("cost").getAsString()).isNotEqualTo(secondJsonAsJsonArray.get("cost").getAsString());
        assertThat(firstJsonAsJsonArray.get("costType").getAsString()).isNotEqualTo(secondJsonAsJsonArray.get("costType").getAsString());
        assertThat(firstJsonAsJsonArray.get("buyDate").getAsString()).isNotEqualTo(secondJsonAsJsonArray.get("costType").getAsString());
        assertThat(firstJsonAsJsonArray.get("buyDate").getAsString()).isNotEqualTo(secondJsonAsJsonArray.get("buyDate").getAsString());
    }

    @Test
    public void testWithInvalidOperation()
    {
        Date timestamp = new Date(Calendar.getInstance().getTimeInMillis());
        String expectedCost = "200";

        JsonObject jsonRequest = createJsonHeader("Put", timestamp, "All");

        JsonObject contentObject = new JsonObject();
        contentObject.addProperty("cost", expectedCost);
        contentObject.addProperty("costType", "bill");
        contentObject.addProperty("comment", "A comment");
        contentObject.addProperty("uuid", "idToBeRemoved");

        JsonObject userObject = createUserObject("remove_me2");

        jsonRequest.add("user", userObject);
        jsonRequest.add("content", contentObject);

        String response = executeNetworkPost(jsonRequest);
        assertThat(response).isNotNull(); //TODO validate more
        validateFailedResponse(response);
    }

    private void validateFailedResponse(String response)
    {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(response).getAsJsonObject();
        assertThat(jsonObject.get("Time").getAsString()).isNotNull();
        assertThat(jsonObject.get("Response").getAsString()).isEqualTo("Failed");
    }

    private void validateRemoveResponse(String response)
    {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(response).getAsJsonObject();

        assertThat(jsonObject.get("Time")).isNotNull();
        assertThat(jsonObject.get("Type").getAsString())
                .contains("Remove")
                .contains("Expense");
        assertThat(jsonObject.get("AffectedRows").getAsInt()).isEqualTo(4);
        assertThat(jsonObject.get("Response").getAsString()).isEqualTo("Success");
    }

    private void validateGetResponse(String response, String expectedCost, int size)
    {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(response).getAsJsonObject();

        assertThat(jsonObject.get("Time").getAsString()).isNotNull();
        assertThat(jsonObject.get("Type").getAsString()).isEqualTo("Get-Expenses");
        assertThat(jsonObject.get("Response").getAsString()).isEqualTo("Success");

        JsonArray jsonArray = jsonObject.getAsJsonArray("Get-Data");
        assertThat(jsonArray).isNotNull();
        assertThat(jsonArray.size()).isEqualTo(size);
        for (JsonElement object : jsonArray)
        {
            JsonObject spot = object.getAsJsonObject();

            assertThat(spot.get("cost").getAsString()).isEqualTo(expectedCost);
            assertThat(spot.get("costType").getAsString()).isEqualTo("bill");
            assertThat(spot.get("buyDate").getAsString())
                    .isNotNull()
                    .isNotEmpty();
            assertThat(spot.get("comment").getAsString())
                    .isNotNull()
                    .isNotEmpty();
        }
    }

    private void validatePutResponse(String response)
    {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(response).getAsJsonObject();

        assertThat(jsonObject.get("Type").getAsString())
                .contains("Put")
                .contains("Expenses");
        assertThat(jsonObject.get("Response").getAsString()).isEqualTo("Success");

        assertThat(jsonObject.get("Time").getAsString()).isNotNull();
    }

}
