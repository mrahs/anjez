/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

  This file is part of Anjez.
  Anjez is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

package pw.ahs.app.anjez.model;

public class TDDuration implements Comparable<TDDuration> {
    public static String formatDuration(long seconds) {

        long year, month, day, hour, minute, second;
        long durationSeconds = seconds;

        long secondsInYear = 31104000;
        year = durationSeconds / secondsInYear;
        durationSeconds = durationSeconds % secondsInYear;

        long secondsInMonth = 2592000;
        month = durationSeconds / secondsInMonth;
        durationSeconds = durationSeconds % secondsInMonth;

        long secondsInDay = 86400;
        day = durationSeconds / secondsInDay;
        durationSeconds = durationSeconds % secondsInDay;

        long secondsInHour = 3600;
        hour = durationSeconds / secondsInHour;
        durationSeconds = durationSeconds % secondsInHour;

        long secondsInMinute = 60;
        minute = durationSeconds / secondsInMinute;
        durationSeconds = durationSeconds % secondsInMinute;

        second = durationSeconds;
        String sMonth = format(month),
                sDay = format(day),
                sHour = format(hour),
                sMinute = format(minute),
                sSecond = format(second);
        if (year != 0)
            return year + "-" + sMonth + "-" + sDay + " " + sHour + ":" + sMinute + ":" + sSecond;
        else if (month != 0)
            return sMonth + "-" + sDay + " " + sHour + ":" + sMinute + ":" + sSecond;
        else if (day != 0)
            return sDay + " " + sHour + ":" + sMinute + ":" + sSecond;
        else if (hour != 0)
            return sHour + ":" + sMinute + ":" + sSecond;
        else if (minute != 0)
            return sMinute + ":" + sSecond;
        else
            return "" + sSecond;
    }

    private static String format(long n) {
        if (n < 10) return "0" + n;
        return "" + n;
    }

    private final long seconds;

    public TDDuration(long seconds) {
        this.seconds = seconds;
    }

    public long getSeconds() {
        return seconds;
    }

    @Override
    public String toString() {
        return formatDuration(seconds);
    }

    @Override
    public int compareTo(TDDuration other) {
        if (seconds > other.seconds)
            return 1;
        else if (seconds == other.seconds)
            return 0;
        else return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TDDuration duration = (TDDuration) o;

        return seconds == duration.seconds;

    }

    @Override
    public int hashCode() {
        return (int) (seconds ^ (seconds >>> 32));
    }
}
