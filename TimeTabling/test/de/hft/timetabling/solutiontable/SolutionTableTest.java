package de.hft.timetabling.solutiontable;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.services.AbstractServicesTest;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.solutiontable.SolutionTable;

/**
 * @author Alexander Weickmann
 */
public class SolutionTableTest extends AbstractServicesTest {

	private ISolutionTableService solutionTable;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		solutionTable = new SolutionTable();
	}

	public void testCreateNewSolution() {
		ICourse[][] coding = new ICourse[instance.getNumberOfPeriods()][instance
				.getNumberOfRooms()];
		ISolution newSolution = solutionTable.createNewSolution(coding,
				instance);
		assertEquals(coding, newSolution.getCoding());

		try {
			coding = new ICourse[instance.getNumberOfPeriods() - 1][instance
					.getNumberOfRooms()];
			newSolution = solutionTable.createNewSolution(coding, instance);
			fail();
		} catch (IllegalArgumentException e) {
			// Expected exception (incomplete coding).
		}

		try {
			coding = new ICourse[instance.getNumberOfPeriods()][instance
					.getNumberOfRooms() - 1];
			newSolution = solutionTable.createNewSolution(coding, instance);
			fail();
		} catch (IllegalArgumentException e) {
			// Expected exception (incomplete coding).
		}
	}

	public void testGetSolution() {
		try {
			solutionTable.getSolution(ISolutionTableService.TABLE_SIZE + 1);
			fail();
		} catch (IndexOutOfBoundsException e) {
			// Expected exception (solution number out of range).
		}

		ISolution newSolution = solutionTable.createNewSolution(
				new ICourse[instance.getNumberOfPeriods()][instance
						.getNumberOfRooms()], instance);
		solutionTable.addSolution(newSolution);
		solutionTable.voteForSolution(0, 100, 100);
		ISolution solution = solutionTable.getSolution(0);
		assertEquals(newSolution, solution);
	}

	public void testToString() {
		assertEquals("Solution Table (" + 0 + " entries)", solutionTable
				.toString());
	}

}
