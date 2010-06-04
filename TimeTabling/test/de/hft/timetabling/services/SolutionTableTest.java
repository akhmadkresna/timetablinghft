package de.hft.timetabling.services;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ISolution;

/**
 * @author Alexander Weickmann
 */
public class SolutionTableTest extends AbstractServicesTest {

	private ISolutionTableService solutionTable;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		solutionTable = new SolutionTable();
		for (int i = 0; i < ISolutionTableService.TABLE_SIZE; i++) {
			ICourse[][] coding = new ICourse[instance.getNumberOfPeriods()][instance
					.getNumberOfRooms()];
			solutionTable.putSolution(i, solutionTable.createNewSolution(
					coding, instance));
		}
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
		solutionTable.putSolution(5, newSolution);
		ISolution solution = solutionTable.getSolution(5);
		assertEquals(newSolution, solution);
	}

	public void testPutSolution() {
		ICourse[][] coding = new ICourse[instance.getNumberOfPeriods()][instance
				.getNumberOfRooms()];
		ISolution newSolution = solutionTable.createNewSolution(coding,
				instance);
		try {
			solutionTable.putSolution(ISolutionTableService.TABLE_SIZE + 1,
					newSolution);
			fail();
		} catch (IndexOutOfBoundsException e) {
			// Expected exception (solution number out of range).
		}

		solutionTable.putSolution(9, newSolution);
		assertEquals(newSolution, solutionTable.getSolution(9));
	}

	public void testVoteForSolution() {
		try {
			solutionTable.voteForSolution(ISolutionTableService.TABLE_SIZE + 1,
					100);
			fail();
		} catch (IndexOutOfBoundsException e) {
			// Expected exception (solution number out of range).
		}

		solutionTable.voteForSolution(5, 200);
		assertEquals(200, solutionTable.getPenaltySumForSolution(5));
	}

	public void testGetPenaltySumForSolution() {
		try {
			solutionTable
					.getPenaltySumForSolution(ISolutionTableService.TABLE_SIZE + 1);
			fail();
		} catch (IndexOutOfBoundsException e) {
			// Expected exception (solution number out of range).
		}

		solutionTable.voteForSolution(5, 200);
		assertEquals(200, solutionTable.getPenaltySumForSolution(5));
	}

	public void testGetBestSolution() {
		ISolutionTableService newSolutionTable = new SolutionTable();
		assertNull(newSolutionTable.getBestSolution());

		solutionTable.voteForSolution(0, 200);
		solutionTable.voteForSolution(1, 500);
		solutionTable.voteForSolution(0, 400);

		ISolution expectedBestSolution = solutionTable.getSolution(1);
		assertEquals(expectedBestSolution, solutionTable.getBestSolution());

		solutionTable.putSolution(0, solutionTable.createNewSolution(
				new ICourse[instance.getNumberOfPeriods()][instance
						.getNumberOfRooms()], instance));
		assertEquals(expectedBestSolution, solutionTable.getBestSolution());

		solutionTable.voteForSolution(5, 100);
		expectedBestSolution = solutionTable.getSolution(5);
		assertEquals(expectedBestSolution, solutionTable.getBestSolution());
	}

	public void testGetBestSolutionPenaltySum() {
		ISolutionTableService newSolutionTable = new SolutionTable();
		try {
			newSolutionTable.getBestSolutionPenaltySum();
			fail();
		} catch (RuntimeException e) {
			// Expected exception.
		}

		solutionTable.voteForSolution(0, 200);
		solutionTable.voteForSolution(1, 500);
		solutionTable.voteForSolution(0, 400);

		assertEquals(500, solutionTable.getBestSolutionPenaltySum());
	}

	public void testToString() {
		assertEquals("Solution Table", solutionTable.toString());
	}

	public void testReplaceWorstSolution() {
		ISolutionTableService newSolutionTable = new SolutionTable();
		ISolution newSolution = newSolutionTable.createNewSolution(
				new ICourse[instance.getNumberOfPeriods()][instance
						.getNumberOfRooms()], instance);
		newSolutionTable.replaceWorstSolution(newSolution);
		assertEquals(newSolution, newSolutionTable.getSolution(0));

		solutionTable.voteForSolution(0, 100);
		solutionTable.voteForSolution(1, 200);
		solutionTable.voteForSolution(2, 300);

		solutionTable.replaceWorstSolution(newSolution);
		assertEquals(newSolution, solutionTable.getSolution(2));
	}
}
