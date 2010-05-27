package de.hft.timetabling.writer;

import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.IWriterService;
import de.hft.timetabling.services.ServiceLocator;

/**
 * Implementation of the writer service.
 * 
 * @author Alexander Weickmann
 * 
 * @see IWriterService
 */
public final class Writer implements IWriterService {

	@Override
	public void outputBestSolution() {
		ISolutionTableService solutionTableService = ServiceLocator
				.getInstance().getSolutionTableService();
		ISolution bestSolution = solutionTableService.getBestSolution();

		// TODO AW: Implement writing of the output file.
		System.out.println(bestSolution);
	}

}
