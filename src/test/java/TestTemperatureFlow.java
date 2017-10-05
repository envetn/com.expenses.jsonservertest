import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import databaseUtils.DateUtils;
import internal.Temperature;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.*;


import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * Created by olof on 2016-07-13.
 */
public class TestTemperatureFlow extends BaseTest
{
    private static String TEMP_FOR_2017_10_02 = "20.2@21.1@24.1@25.5@23.1@121";
    private static String TIMESTAMP_FOR_2017_10_02 = "15:30:51@15:32:31@15:38:30@17:09:08@17:09:33@17:11:34";

    private static String TEMP_FOR_2017_10_07 = "30.2@20.1@24.1@12.5@23.1@121";
    private static String TIMESTAMP_FOR_2017_10_07 = "11:30:51@12:32:31@15:38:30@23:09:08@17:09:33@17:11:34";

    private static String TEMP_FOR_2017_12_01 = "20.2@21.1@24.1@25.5@23.1@121";
    private static String TIMESTAMP_FOR_2017_12_01 = "15:30:51@15:32:31@15:38:30@17:09:08@17:09:33@17:11:34";

    private static String TEMP_FOR_2016_05_21 = "20.2@21.1@24.1@25.5@23.1@121";
    private static String TIMESTAMP_FOR_2017_12_11 = "15:30:51@15:32:31@15:38:30@17:09:08@17:09:33@17:11:34";

    private static String TEMP_FOR_2017_12_11 = "25.2@25.1@25.1@25.5@23.1@25.1";
    private static String TIMESTAMP_FOR_2016_05_21 = "15:30:51@15:32:31@15:38:30@17:09:08@17:09:33@17:11:34";

    private static Date DATE_2017_10_02 = Date.valueOf(("2017-10-02"));
    private static Date DATE_2017_10_07 = Date.valueOf(("2017-10-07"));
    private static Date DATE_2017_12_01 = Date.valueOf(("2017-12-01"));
    private static Date DATE_2017_12_11 = Date.valueOf(("2017-12-11"));
    private static Date DATE_2016_05_21 = Date.valueOf(("2016-05-21"));

    private static Temperature TEMP_2017_10_02 = new Temperature(DATE_2017_10_02, TEMP_FOR_2017_10_02, TIMESTAMP_FOR_2017_10_02);
    private static Temperature TEMP_2017_10_11 = new Temperature(DATE_2017_10_07, TEMP_FOR_2017_10_07, TIMESTAMP_FOR_2017_10_07);
    private static Temperature TEMP_2017_12_01 = new Temperature(DATE_2017_12_01, TEMP_FOR_2017_12_01, TIMESTAMP_FOR_2017_12_01);
    private static Temperature TEMP_2017_12_11 = new Temperature(DATE_2017_12_11, TEMP_FOR_2017_12_11, TIMESTAMP_FOR_2017_12_11);
    private static Temperature TEMP_2016_05_21 = new Temperature(DATE_2016_05_21, TEMP_FOR_2016_05_21, TIMESTAMP_FOR_2016_05_21);

    @BeforeClass
    public static void setUpTestData() throws SQLException
    {
        DATABSAE_HELPER.insertTemperatureValues(Arrays.asList(TEMP_2017_10_02, TEMP_2017_10_11, TEMP_2017_12_01, TEMP_2017_12_11, TEMP_2016_05_21));
    }

    @AfterClass
    public static void cleanUp() throws SQLException
    {
        DATABSAE_HELPER.removeTemperatureValues(Arrays.asList(TEMP_2017_10_02, TEMP_2017_10_11, TEMP_2017_12_01, TEMP_2017_12_11, TEMP_2016_05_21));
    }

    @Test
    public void testCreateTemperature()
    {
        Date timestamp = Date.valueOf(("2017-09-30"));
        SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
        String formattedDate = time.format(timestamp);

        JsonObject jsonRequest = createJsonHeader("Put", timestamp, "Temperature");
        JsonObject userObject = createUserObject("remove_me2");

        jsonRequest.add("user", userObject);
        jsonRequest.addProperty("temperature", "20.5");
        jsonRequest.addProperty("time", formattedDate);
        String response = executeNetworkPost(jsonRequest);
        validatePutResponse(response);

        System.out.println(response);
    }

    @Test
    public void testGetTemperature() throws ParseException
    {
        JsonObject jsonRequest = createGetRequestNew("Temperature", "date", "timePeriod", "All", DateUtils.getBoTEoT());
        JsonObject userObject = createUserObject("remove_me2");

        jsonRequest.add("user", userObject);

        String response = executeNetworkPost(jsonRequest);
        validateGetResponse(response);
    }

    @Test
    public void testGetTemperatureByDate() throws ParseException
    {
        JsonObject jsonRequestDay1 = createGetRequestNew("Temperature", "date", "timePeriod", "All", DateUtils.getSingleDayBy(2017, 10, 2));

        JsonObject userObject = createUserObject("remove_me2");

        jsonRequestDay1.add("user", userObject);

        String responseDay1 = executeNetworkPost(jsonRequestDay1);
        validateGetResponse(responseDay1, Collections.singletonList(TEMP_2017_10_02));
        System.out.println(responseDay1);

    }

    @Test
    public void testGetMonthlyTemperature() throws ParseException
    {
        Map<String, LocalDate> minMax = DateUtils.getFirstAndLastDayOf(12, 2017);
        JsonObject jsonRequest = createGetRequestNew("Temperature", "date", "timePeriod", "12", minMax);
        JsonObject userObject = createUserObject("remove_me2");

        jsonRequest.add("user", userObject);
        String response = executeNetworkPost(jsonRequest);
        validateGetResponse(response, Arrays.asList(TEMP_2017_12_01, TEMP_2017_12_11));
    }

    @Test
    public void testGetWeeklyTemperature() throws ParseException
    {
        Map<String, LocalDate> minMax = new HashMap<>();

        LocalDate now = LocalDate
                .now()
                .withYear(2017)
                .withMonth(10)
                .withDayOfMonth(2);

        TemporalField fieldISO = WeekFields.of(Locale.FRENCH).dayOfWeek();

        LocalDate monday = now.with(fieldISO, 1); // 2016-12-12 (Monday)
        LocalDate sunday = now.with(fieldISO, 7);

        minMax.put("first", monday);
        minMax.put("last", sunday);


        JsonObject jsonRequest = createGetRequestNew("Temperature", "date", "timePeriod", "12", minMax);
        JsonObject userObject = createUserObject("remove_me2");

        jsonRequest.add("user", userObject);
        String response = executeNetworkPost(jsonRequest);
        validateGetResponse(response, Arrays.asList(TEMP_2017_10_02, TEMP_2017_10_11));
        validateGetResponse(response);
    }


    private void validateGetResponse(String response, List<Temperature> temperatures)
    {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(response).getAsJsonObject();

        assertThat(jsonObject.get("Type").getAsString()).isEqualTo("GET");
        assertThat(jsonObject.get("Reason").getAsString()).isEqualTo("Get-Temperature");
        assertThat(jsonObject.get("Response").getAsString()).isEqualTo("Success");
        JsonArray jsonArray = jsonObject.getAsJsonArray("Get-Data");
        assertThat(jsonArray.size()).isNotEqualTo(0);

        for(int i=0; i<jsonArray.size(); i++)
        {
            JsonElement jsonElement = jsonArray.get(i);
            JsonObject asJsonObject = jsonElement.getAsJsonObject();
            String actualDate = asJsonObject.get("date").getAsString();
            String actualTemperature = asJsonObject.get("temperature").getAsString();
            String actualTime = asJsonObject.get("time").getAsString();

            Temperature expectedTemperature = temperatures.get(i);

            Date myDate = expectedTemperature.getMyDate();
            String expectedTemperatureS = expectedTemperature.getMyTemperatures();
            String expectedTime = expectedTemperature.getMyTimestamps();

            assertThat(myDate.toString()).isEqualTo(actualDate);
            assertThat(expectedTemperatureS).isEqualTo(actualTemperature);
            assertThat(expectedTime).isEqualTo(actualTime);
        }
    }

    private void validateGetResponse(String response)
    {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(response).getAsJsonObject();

        assertThat(jsonObject.get("Type").getAsString()).isEqualTo("GET");
        assertThat(jsonObject.get("Reason").getAsString()).isEqualTo("Get-Temperature");
        assertThat(jsonObject.get("Response").getAsString()).isEqualTo("Success");
        JsonArray jsonArray = jsonObject.getAsJsonArray("Get-Data");
        assertThat(jsonArray.size()).isNotEqualTo(0);

        //todo validate better
        for (JsonElement object : jsonArray)
        {
            JsonObject spot = object.getAsJsonObject();

            assertThat(spot.get("temperature").getAsString()).isNotNull();
            assertThat(spot.get("date").getAsString()).isNotNull();
            assertThat(spot.get("time").getAsString()).isNotNull();
        }

//List<Double> averageTemp = new ArrayList<>(jsonArray.size());
//        parseTemp(averageTemp, spot);
//        double average = 0.0;
//        for(Double myValue : averageTemp)
//        {
//            average += myValue;
//        }
//
//        average /= averageTemp.size();
//        System.out.println("average temperature : " + average);

    }

    private void parseTemp(List<Double> averageTemp, JsonObject spot)
    {
        String temperatureAsString = spot.get("temperature").getAsString();

        String[] spitedTemp = temperatureAsString.split("@");

        for (String temp : spitedTemp)
        {
            averageTemp.add(Double.valueOf(temp));
        }
    }

    private void validatePutResponse(String response)
    {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(response).getAsJsonObject();

        assertThat(jsonObject.get("Type").getAsString()).isEqualTo("PUT");
        assertThat(jsonObject.get("Reason").getAsString()).isEqualTo("Put-Temperature");
        assertThat(jsonObject.get("Response").getAsString()).isEqualTo("Success");
        assertThat(jsonObject.get("Time").getAsString()).isNotNull();
    }
}
