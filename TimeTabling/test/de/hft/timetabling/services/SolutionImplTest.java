package de.hft.timetabling.services;

import junit.framework.TestCase;
import de.hft.timetabling.common.ISolution;

/**
 * @author Alexander Weickmann
 */
public class SolutionImplTest extends TestCase {

	private static final String[][] CODING = new String[][] {};

	private ISolution solution;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		solution = new SolutionImpl(CODING);
	}

	public void testGetCoding() {
		assertEquals(CODING, solution.getCoding());
	}

}
