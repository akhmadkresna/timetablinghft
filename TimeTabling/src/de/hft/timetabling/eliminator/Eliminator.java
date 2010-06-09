package de.hft.timetabling.eliminator;

import de.hft.timetabling.services.IEliminatorService;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.ServiceLocator;

/**
 * @author Alexander Weickmann
 */
public final class Eliminator implements IEliminatorService {

	/** The percentage of the population to eliminate. */
	private static final int PERCENTAGE = 0;

	/**
	 * Eliminates a percentage of the worst solutions from the table.
	 */
	@Override
	public void eliminateSolutions() {
		ISolutionTableService solutionTable = ServiceLocator.getInstance()
				.getSolutionTableService();
		int nrSolutionToEliminate = (PERCENTAGE * ISolutionTableService.TABLE_SIZE) / 100;

		System.out.print("ELIMINATOR: Eliminating worst " + PERCENTAGE
				+ "% of solutions (" + nrSolutionToEliminate + ") ...");

		for (int i = 0; i < nrSolutionToEliminate; i++) {
			solutionTable.removeWorstSolution();
		}

		System.out.print(" done.\n");
	}

}
