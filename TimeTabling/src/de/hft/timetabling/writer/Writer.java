package de.hft.timetabling.writer;

import de.hft.timetabling.common.ICourse;
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

		ICourse[][] coding = bestSolution.getCoding();
		for (int period = 0; period < coding.length; period++) {
			ICourse[] coursesInPeriod = coding[period];
			for (int room = 0; room < coursesInPeriod.length; room++) {
				// TODO AW
			}
		}
	}

}
