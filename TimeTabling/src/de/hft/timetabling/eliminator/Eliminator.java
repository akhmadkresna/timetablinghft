package de.hft.timetabling.eliminator;

import java.util.List;

import de.hft.timetabling.common.ISolution;
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
		List<ISolution> orderedSolutions = solutionTable
				.getSolutionsOrderedByPenalty();

		// The worst solutions are at the end of the ordered list.
		int halfSize = orderedSolutions.size() / 2;
		for (int i = halfSize - 1; i < halfSize; i++) {
			ISolution solution = orderedSolutions.get(i);
			solutionTable.removeSolution(solution);
		}

		System.out.print(" done.\n");
	}

}
