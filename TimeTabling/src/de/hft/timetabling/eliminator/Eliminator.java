package de.hft.timetabling.eliminator;

import de.hft.timetabling.services.IEliminatorService;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.ServiceLocator;

/**
 * @author Alexander Weickmann
 */
public final class Eliminator implements IEliminatorService {

	/**
	 * Eliminates the 50% worst solutions from the table.
	 */
	@Override
	public void eliminateSolutions() {
		System.out.print("ELIMINATOR: Eliminating worst 50% of solutions ...");

		ISolutionTableService solutionTable = ServiceLocator.getInstance()
				.getSolutionTableService();
		int halfSize = solutionTable.getSize() / 2;
		for (int i = 0; i < halfSize; i++) {
			solutionTable.removeWorstSolution();
		}

		System.out.print(" done.\n");
	}

}
