package de.hft.timetabling.util;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * This class provides utility methods concerning dates.
 * 
 * @author Alexander Weickmann
 */
public class DateUtil {

	/**
	 * @author Matthias Ruszala
	 */
	public static String toTimeString(long timeInMillis) {
		final long hours = TimeUnit.MILLISECONDS.toHours(timeInMillis);
		final long minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis)
				- TimeUnit.HOURS.toMinutes(hours);
		final long seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis)
				- TimeUnit.MINUTES.toSeconds(minutes);

		return String.format("%d h, %d m, %d s", hours, minutes, seconds);
	}

	public static String getTimeStamp(Date date) {
		String dateString = date.toString();
		dateString = dateString.replaceAll(" ", "-");
		dateString = dateString.replaceAll(":", "-");
		return dateString;
	}

}
