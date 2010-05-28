package de.hft.timetabling.services;

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
			String[][] coding = new String[][] { { "" + i } };
			solutionTable.putSolution(i, solutionTable.createNewSolution(
					coding, instance));
		}
	}

	public void testCreateNewSolution() {
		String[][] coding = new String[][] {};
		ISolution newSolution = solutionTable.createNewSolution(coding,
				instance);
		assertEquals(coding, newSolution.getCoding());
	}

	public void testGetSolution() {
		try {
			solutionTable.getSolution(ISolutionTableService.TABLE_SIZE + 1);
			fail();
		} catch (IndexOutOfBoundsException e) {
			// Expected exception.
		}

		ISolution solution = solutionTable.getSolution(5);
		assertEquals("5", solution.getCoding()[0][0]);
	}

	public void testSetSolution() {
		String[][] coding = new String[][] { { "new" } };
		ISolution newSolution = solutionTable.createNewSolution(coding,
				instance);
		try {
			solutionTable.putSolution(ISolutionTableService.TABLE_SIZE + 1,
					newSolution);
			fail();
		} catch (IndexOutOfBoundsException e) {
			// Expected exception.
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
			// Expected exception.
		}

		solutionTable.voteForSolution(5, 200);
		assertEquals(200, solutionTable.getVoteSumForSolution(5));
	}

	public void testGetVoteSumForSolution() {
		try {
			solutionTable
					.getVoteSumForSolution(ISolutionTableService.TABLE_SIZE + 1);
			fail();
		} catch (IndexOutOfBoundsException e) {
			// Expected exception.
		}

		solutionTable.voteForSolution(5, 200);
		assertEquals(200, solutionTable.getVoteSumForSolution(5));
	}

	public void testGetBestSolution() {
		ISolutionTableService newSolutionTable = new SolutionTable();
		assertNull(newSolutionTable.getBestSolution());

		solutionTable.voteForSolution(0, 200);
		solutionTable.voteForSolution(1, 500);
		solutionTable.voteForSolution(0, 400);

		ISolution expectedBestSolution = solutionTable.getSolution(0);
		assertEquals(expectedBestSolution, solutionTable.getBestSolution());

		solutionTable.putSolution(0, solutionTable.createNewSolution(
				new String[][] { { "new" } }, instance));
		assertEquals(expectedBestSolution, solutionTable.getBestSolution());

		solutionTable.voteForSolution(5, 1000);
		expectedBestSolution = solutionTable.getSolution(5);
		assertEquals(expectedBestSolution, solutionTable.getBestSolution());
	}

	public void testGetBestSolutionVoteSum() {
		ISolutionTableService newSolutionTable = new SolutionTable();
		assertEquals(0, newSolutionTable.getBestSolutionVoteSum());

		solutionTable.voteForSolution(0, 200);
		solutionTable.voteForSolution(1, 500);
		solutionTable.voteForSolution(0, 400);

		assertEquals(600, solutionTable.getBestSolutionVoteSum());
	}

	public void testToString() {
		assertEquals("Solution Table", solutionTable.toString());
	}

}
