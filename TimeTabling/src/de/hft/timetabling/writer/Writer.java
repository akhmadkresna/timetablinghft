package de.hft.timetabling.writer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.IRoom;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.IWriterService;
import de.hft.timetabling.services.ServiceLocator;
import de.hft.timetabling.util.PeriodUtil;

/**
 * Implementation of the writer service.
 * 
 * @author Alexander Weickmann
 * 
 * @see IWriterService
 */
public final class Writer implements IWriterService {

	@Override
	public void outputBestSolution() throws IOException {
		ISolutionTableService solutionTableService = ServiceLocator
				.getInstance().getSolutionTableService();
		ISolution bestSolution = solutionTableService.getBestSolution();
		IProblemInstance problemInstance = bestSolution.getProblemInstance();

		String fileName = problemInstance.getFileName();
		fileName = fileName.substring(0, fileName.lastIndexOf('.'));
		long timestamp = System.currentTimeMillis() / 1000;
		FileWriter fileWriter = new FileWriter("output/" + fileName + "_"
				+ timestamp + ".ctt");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		ICourse[][] coding = bestSolution.getCoding();
		for (int period = 0; period < problemInstance.getNumberOfPeriods(); period++) {
			ICourse[] coursesInPeriod = coding[period];
			for (int roomNumber = 0; roomNumber < problemInstance
					.getNumberOfRooms(); roomNumber++) {

				ICourse course = coursesInPeriod[roomNumber];
				IRoom room = problemInstance.getRoomByUniqueNumber(roomNumber);
				int periodsPerDay = problemInstance.getPeriodsPerDay();
				int day = PeriodUtil
						.getDayFromPeriodOnly(period, periodsPerDay);
				int convertedPeriod = PeriodUtil.convertToDayPeriod(period,
						periodsPerDay);

				bufferedWriter.write(course.getId() + " " + room.getId() + " "
						+ day + " " + convertedPeriod);
				bufferedWriter.newLine();
			}
		}

		bufferedWriter.close();
	}
}
