package de.hft.timetabling.services;

import de.hft.timetabling.common.IEvaluator;
import de.hft.timetabling.common.ISolution;

public final class Evaluator implements IEvaluator {
	public Evaluator() {
		// TODO Auto-generated constructor stub
	}

	private ISolutionTableService solutionTable;

	/**
	 * The solution table is implemented as a map which assigns a unique number
	 * to each solution.
	 */
	@Override
	public void HandInSolution(ISolution solution) {

		int iTableSize;
		// Create the solutionTable and instantiate it
		solutionTable = ServiceLocator.getInstance().getSolutionTableService();

		// Get the table size
		iTableSize = ISolutionTableService.TABLE_SIZE;
		// successful insertion of the solution
		boolean bInsertSuccessful = false;

		// Insert the solution into empty spaces in the Solution Table
		for (int i = 0; i < iTableSize; i++) {
			if (solutionTable.getSolution(i) != null) {
				solutionTable.putSolution(i, solution);
				bInsertSuccessful = true;
				break;
			}
		}
		if (bInsertSuccessful == false) {
			System.out
					.println("Solution Table is full. Running Table clean up procedure... ");
			/*
			 * Call the method to clean the solution table
			 * 
			 * TO BE IMPLEMENTED
			 */
		}

	}

}
