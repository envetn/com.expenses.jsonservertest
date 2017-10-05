package databaseUtils;


import internal.Temperature;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static java.lang.System.exit;
import static java.lang.System.in;

/**
 * Created by olof on 2016-10-27.
 */
public class DatabaseTestHelper
{
    //    private static final String SQL_GET_STRING2 = "SELECT * FROM (SELECT * FROM test.Expenses ORDER BY id) WHERE ROWNUM BETWEEN ? AND ?";
    private static final String EXPENSES_TABLE = "expenses";
    private static final String TEMPERATURE_TABLE = "temperature";
    private static final String SQL_GET_STRING = "SELECT * FROM %s ORDER BY ? LIMIT ?, ?";
    private static final String SQL_GET_STRING_PERIOD = "SELECT * FROM %s WHERE buyDate BETWEEN ? AND ? ORDER BY ?";
    private static final String SQL_GET_ALL = "SELECT * FROM %s ORDER BY ?";
    private static final String SQL_SELECT_HIGHEST_ID = "SELECT id FROM test.%s ORDER BY id DESC LIMIT 1";
    private static final String SQL_CREATE_TABLE_IF_NEEDED = "CREATE TABLE IF NOT EXISTS %s" +
            "(id int(11) AUTO_INCREMENT, " +
            " cost varchar(100) NOT NULL, " +
            " costType varchar(255) NOT NULL, " +
            " buyDate DATE NOT NULL, " +
            " comment varchar(255), " +
            " uuid VARCHAR(100)," +
            " PRIMARY KEY (id) "
            + ")";


    private static final String SQL_REMOVE_STRING = "DELETE FROM %s WHERE uuid=? LIMIT 1";

    private Connection connect;
    private Statement statement;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private static Date myDate;
    private static boolean hasNewData = false;

    // http://www.vogella.com/tutorials/MySQLJava/article.html
    public DatabaseTestHelper()
    {
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost/test?user=root&password=root");
            statement = connect.createStatement();
        }
        catch (SQLException | ClassNotFoundException e)
        {
            System.out.println("DatabaseConnection ." + e.getClass() + "\n" + e.toString());
        }
    }

    public int insertExpensesValues(Map<String, String> content, String username) throws SQLException
    {
        return insertExpensesValues(content, username, content.get("buyDate"));
    }

    public int insertExpensesValues(Map<String, String> content, String username, String date) throws SQLException
    {

        String query = "INSERT INTO test.%s (cost, costType, buyDate, comment, uuid, username) VALUES (?,?,?,?,?,?)";
        String sql = String.format(query, EXPENSES_TABLE);
        preparedStatement = connect.prepareStatement(sql);

        preparedStatement.setString(1, content.get("cost"));
        preparedStatement.setString(2, content.get("costType"));
        preparedStatement.setString(3, date);
        preparedStatement.setString(4, content.get("comment"));
        preparedStatement.setString(5, content.get("uuid"));
        preparedStatement.setString(6, username);

        return preparedStatement.executeUpdate();
    }

    public int insertThreshold(int threshold, int value, String username, String type, int month) throws SQLException
    {
        String query = "INSERT INTO test.threshold (type, month, currentCost, threshold, username) VALUES (?,?,?,?,?)";

        preparedStatement = connect.prepareStatement(query);
        preparedStatement.setString(1, type);
        preparedStatement.setInt(2, month);
        preparedStatement.setInt(3, value);
        preparedStatement.setInt(4, threshold);
        preparedStatement.setString(5, username);

        return preparedStatement.executeUpdate();
    }

    public boolean removeContent()
    {
        try
        {
            String sql = "DELETE FROM test.Expenses_remove_me WHERE uuid=? LIMIT 1";
            preparedStatement = connect.prepareStatement(sql);//+ "'" + id + "'");
            preparedStatement.setString(1, "idToBeRemoved");
            int sqlResult = preparedStatement.executeUpdate();
            return sqlResult == 1;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    public void addUser(String username)
    {
        Date timestamp = new Date(Calendar.getInstance().getTimeInMillis());
        try
        {
            String sql = "INSERT INTO test.expenseuser (username, passwd, created) VALUES (?,?,?)";
            preparedStatement = connect.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, username);
            preparedStatement.setDate(3, timestamp);

            preparedStatement.executeUpdate();
            System.out.println("Creating new test.expenseuser: : " + username);
        }
        catch (SQLException e)
        {
            System.out.println("Failed to find or create new username: Expenses_remove_me");
            e.printStackTrace();
            System.out.println(e.getMessage());
            exit(0);
        }
    }

    public void clearUsernameAndData(String username)
    {
        System.out.println("Remove test-username for expenses: " + username);

        try
        {
            String sql = "DELETE FROM " + EXPENSES_TABLE + " WHERE username=?;";
            preparedStatement = connect.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();

            sql = "DELETE FROM test.expenseuser WHERE username=?";
            preparedStatement = connect.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();

            sql = "DELETE FROM test.threshold WHERE username=?";
            preparedStatement = connect.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();
        }
        catch (SQLException e)
        {
            System.out.println("*** Failed to remove username: " + username + " ***");
            System.out.println(e.getMessage());
        }
    }

    public boolean setupUser(Date triggerTime, String remove_me)
    {

        try
        {
            String sql = "INSERT INTO test.expenseuser (username, passwd, created) VALUES (?,?,?)";
            preparedStatement = connect.prepareStatement(sql);
            preparedStatement.setString(1, remove_me);
            preparedStatement.setString(2, remove_me);
            preparedStatement.setDate(3, triggerTime);

            preparedStatement.executeUpdate();
            System.out.println("Creating new user: remove_me3");
        }
        catch (SQLException e)
        {
            System.out.println("*** Failed to Create user: remove_me3 ***");
            System.out.println(e.getMessage());
            return false;
        }

        return true;

    }

    public void removeUser(List<String> usersToRemove)
    {
        try
        {
            for (String user : usersToRemove)
            {
                String sql = "DELETE FROM test.expenseuser WHERE username=? LIMIT 1";
                preparedStatement = connect.prepareStatement(sql);//+ "'" + id + "'");
                preparedStatement.setString(1, user);
                preparedStatement.executeUpdate();
                System.out.println("Remove user: " + user);
            }

        }
        catch (SQLException e)
        {
            System.out.println("*** Failed to remove user: ***");
            System.out.println(e.getMessage());
        }
    }

    public void insertTemperatureValues(List<Temperature> temperatureList) throws SQLException
    {

        System.out.println("Inserting temperature values into db...");
        String query = "INSERT INTO test.temperature (date, temperatur, time) VALUES (?,?,?)";

        for (Temperature temperature : temperatureList)
        {
            String temperatures = temperature.getMyTemperatures();
            String timestamps = temperature.getMyTimestamps();
            preparedStatement = connect.prepareStatement(query);

            preparedStatement.setDate(1, temperature.getMyDate());
            preparedStatement.setString(2, temperatures);
            preparedStatement.setString(3, timestamps);

            int i1 = preparedStatement.executeUpdate();

            if (i1 == 0)
            {
                //faield
            }

        }
    }

    public void removeTemperatureValues(List<Temperature> temperatures) throws SQLException
    {
        System.out.println("Deleting temperature values from db... ");

        for (Temperature temperature : temperatures)
        {
            String sql = "DELETE FROM " + "temperature" + " WHERE date=?";
            preparedStatement = connect.prepareStatement(sql);
            preparedStatement.setDate(1, temperature.getMyDate());
            preparedStatement.executeUpdate();
        }
    }
}
