package com.microsoft.azure.cosmosdb.sql;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

//https://docs.microsoft.com/en-us/azure/cosmos-db/working-with-dates
public class DateTimeUtil {

    public static DateTime GetCurrentUtcDateTime()
    {
        return new DateTime(DateTimeZone.UTC);
    }

    public static DateTime GetDateTimeWithMinsIntervalStamp(DateTime dateTime, int interval)
    {
        String newDateTimeString=GetCosmosDBIso8601Format(dateTime);
        DateTime newDateTime=ConvertUtcIso8601FormatStringToDate(newDateTimeString);
        int mins=newDateTime.getMinuteOfHour();
        int seconds=newDateTime.getSecondOfMinute();
        int milliSeconds=newDateTime.getMillisOfSecond();

        // Remove time except hours
        newDateTime=newDateTime.minusMinutes(mins);
        newDateTime=newDateTime.minusSeconds(seconds);
        newDateTime=newDateTime.minusMillis(milliSeconds);

        // calculate interval time stamp
        int intervalStamp=(mins/interval)*interval;
        newDateTime=newDateTime.plusMinutes(intervalStamp);
        return newDateTime;
    }

    public static String GetCosmosDBIso8601Format(DateTime dateTime)
    {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        return fmt.print(dateTime);
    }

    public static DateTime ConvertUtcIso8601FormatStringToDate(String isoFormat8601DateTime)
    {
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        return fmt.withZone(DateTimeZone.UTC).parseDateTime(isoFormat8601DateTime);
    }
}
