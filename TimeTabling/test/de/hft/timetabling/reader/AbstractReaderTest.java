package de.hft.timetabling.reader;

import junit.framework.TestCase;

/**
 * @author Alexander Weickmann
 */
public abstract class AbstractReaderTest extends TestCase {

	protected static final String FILE_NAME = "test.ctt";

	protected static final String NAME = "Test Instance";

	protected static final int NUMBER_OF_COURSES = 3;

	protected static final int NUMBER_OF_ROOMS = 2;

	protected static final int NUMBER_OF_DAYS = 2;

	protected static final int PERIODS_PER_DAY = 3;

	protected static final int NUMBER_OF_CURRICULA = 2;

	protected static final int NUMBER_OF_CONSTRAINTS = 2;

	protected ProblemInstanceImpl instance;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		instance = new ProblemInstanceImpl(FILE_NAME, NAME, NUMBER_OF_COURSES,
				NUMBER_OF_ROOMS, NUMBER_OF_DAYS, PERIODS_PER_DAY,
				NUMBER_OF_CURRICULA, NUMBER_OF_CONSTRAINTS);
	}

}
