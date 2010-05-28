package de.hft.timetabling.services;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ISolution;

/**
 * @author Alexander Weickmann
 */
public class SolutionImplTest extends AbstractServicesTest {

	private static final ICourse[][] CODING = new ICourse[][] {};

	private ISolution solution;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		solution = new SolutionImpl(CODING, instance);
	}

	public void testGetProblemInstance() {
		assertEquals(instance, solution.getProblemInstance());
	}

	public void testGetCoding() {
		assertEquals(CODING, solution.getCoding());
	}

}