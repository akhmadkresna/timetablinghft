package de.hft.timetabling.solutiontable;

import java.util.HashSet;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.services.AbstractServicesTest;

/**
 * @author Alexander Weickmann
 */
public class SolutionImplTest extends AbstractServicesTest {

	private static final ICourse[][] CODING = new ICourse[][] {};

	private ISolution solution;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		solution = new SolutionImpl(CODING, new HashSet<ISolution>(), instance);
	}

	public void testGetProblemInstance() {
		assertEquals(instance, solution.getProblemInstance());
	}

	public void testGetCoding() {
		assertEquals(CODING, solution.getCoding());
	}

}
