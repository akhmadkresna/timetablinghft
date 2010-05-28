package de.hft.timetabling.util;

import junit.framework.TestCase;

/**
 * @author Alexander Weickmann
 */
public class PeriodUtilTest extends TestCase {

	private static final int PERIODS_PER_DAY = 3;

	public void testConvertToPeriodOnly() {
		assertEquals(0, PeriodUtil.convertToPeriodOnly(0, 0, PERIODS_PER_DAY));
		assertEquals(2, PeriodUtil.convertToPeriodOnly(0, 2, PERIODS_PER_DAY));
		assertEquals(5, PeriodUtil.convertToPeriodOnly(1, 2, PERIODS_PER_DAY));
	}

	public void testConvertToDayPeriod() {
		assertEquals(1, PeriodUtil.convertToDayPeriod(1, PERIODS_PER_DAY));
		assertEquals(0, PeriodUtil.convertToDayPeriod(3, PERIODS_PER_DAY));
		assertEquals(1, PeriodUtil.convertToDayPeriod(4, PERIODS_PER_DAY));
		assertEquals(2, PeriodUtil.convertToDayPeriod(5, PERIODS_PER_DAY));
	}

	public void testGetDayFromPeriodOnly() {
		assertEquals(0, PeriodUtil.getDayFromPeriodOnly(0, PERIODS_PER_DAY));
		assertEquals(0, PeriodUtil.getDayFromPeriodOnly(1, PERIODS_PER_DAY));
		assertEquals(0, PeriodUtil.getDayFromPeriodOnly(2, PERIODS_PER_DAY));
		assertEquals(1, PeriodUtil.getDayFromPeriodOnly(3, PERIODS_PER_DAY));
		assertEquals(1, PeriodUtil.getDayFromPeriodOnly(4, PERIODS_PER_DAY));
		assertEquals(1, PeriodUtil.getDayFromPeriodOnly(5, PERIODS_PER_DAY));
		assertEquals(2, PeriodUtil.getDayFromPeriodOnly(6, PERIODS_PER_DAY));
	}

}
