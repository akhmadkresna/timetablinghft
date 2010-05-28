package de.hft.timetabling.util;

/**
 * This is a utility class providing methods to convert the day-period format
 * used by the competition's input files to a period-only format and vice versa.
 * 
 * @author Alexander Weickmann
 */
public final class PeriodUtil {

	/**
	 * Converts a period given in the day-period format used by the
	 * competition's input format to a period-only format and returns the
	 * result.
	 * 
	 * @param day
	 *            The zero-based number of the day.
	 * @param period
	 *            The zero-based number of the period (day-period format).
	 * @param periodsPerDay
	 *            Specifies how many periods one day has.
	 */
	public static int convertToPeriodOnly(int day, int period, int periodsPerDay) {
		return period + day * periodsPerDay;
	}

	/**
	 * Converts a period given in the period-only format to the day-period
	 * format used by the competition and returns the result.
	 * 
	 * @param period
	 *            The zero-based number of the period (period-only format).
	 * @param periodsPerDay
	 *            Specifies how many periods one day has.
	 */
	public static int convertToDayPeriod(int period, int periodsPerDay) {
		return period % periodsPerDay;
	}

	/**
	 * Returns the number of the day for a period given in the period-only
	 * format.
	 * 
	 * @param period
	 *            The zero-based number of the period (period-only format).
	 * @param periodsPerDay
	 *            Specifies how many periods one day has.
	 */
	public static int getDayFromPeriodOnly(int period, int periodsPerDay) {
		int day = 0;
		for (int i = 1; i <= period; i++) {
			if (i % periodsPerDay == 0) {
				day++;
			}
		}
		return day;
	}

	private PeriodUtil() {
		// Utility class not to be instantiated.
	}

}
