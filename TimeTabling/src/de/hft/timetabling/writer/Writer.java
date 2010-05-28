package de.hft.timetabling.writer;

import de.hft.timetabling.common.IProblemInstance;
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
		IProblemInstance problemInstance = bestSolution.getProblemInstance();

		// ICourse[][] coding = bestSolution.getCoding();
		int numberOfPeriods = problemInstance.getNumberOfDays()
				* problemInstance.getPeriodsPerDay();
		for (int period = 0; period < numberOfPeriods; period++) {
			// ICourse[] coursesInPeriod = coding[period];
			for (int room = 0; room < problemInstance.getNumberOfRooms(); room++) {
				// TODO AW
			}
		}
	}

}
