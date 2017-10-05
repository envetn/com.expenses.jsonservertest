package internal;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

/**
 * Created by lofie on 2017-09-30.
 */
public class Temperature
{
    Date myDate;
    String myTemperatures;
    String myTimestamps;

    public Date getMyDate()
    {
        return myDate;
    }

    public String getMyTemperatures()
    {
        return myTemperatures;
    }

    public String getMyTimestamps()
    {
        return myTimestamps;
    }

    public Temperature(Date date, String temperatures, String timestamps)
    {
        myDate = date;
        myTemperatures = temperatures;
        myTimestamps = timestamps;
    }
}
