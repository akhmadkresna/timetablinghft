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
		ISolution bestSolution = solutionTableService.getBestPenaltySolution();
		IProblemInstance problemInstance = bestSolution.getProblemInstance();

		long timestamp = System.currentTimeMillis() / 1000;
		String fileName = problemInstance.getFileName();
		fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
		fileName = fileName.substring(0, fileName.lastIndexOf('.'));
		fileName = "output/" + fileName + "_" + timestamp + ".ctt";
		outputSolution(fileName, bestSolution, problemInstance);
	}

	@Override
	public void outputSolution(String fileName, ISolution solution,
			IProblemInstance problemInstance) throws IOException {

		System.out.print("WRITER: Writing best solution to '" + fileName + "'");

		FileWriter fileWriter = new FileWriter(fileName);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		ICourse[][] coding = solution.getCoding();
		int numberOfPeriods = problemInstance.getNumberOfPeriods();
		int numberOfRooms = problemInstance.getNumberOfRooms();
		for (int period = 0; period < numberOfPeriods; period++) {
			ICourse[] coursesInPeriod = coding[period];
			for (int roomNumber = 0; roomNumber < numberOfRooms; roomNumber++) {

				ICourse course = coursesInPeriod[roomNumber];
				// Continue if no assignment at this location.
				if (course == null) {
					continue;
				}

				IRoom room = problemInstance.getRoomByUniqueNumber(roomNumber);
				int periodsPerDay = problemInstance.getPeriodsPerDay();
				int day = PeriodUtil
						.getDayFromPeriodOnly(period, periodsPerDay);
				int convertedPeriod = PeriodUtil.convertToDayPeriod(period,
						periodsPerDay);

				bufferedWriter.write(course.getId() + " " + room.getId() + " "
						+ day + " " + convertedPeriod);
				// Append new line if not at end of file.
				if (!((period == numberOfPeriods - 1) && (roomNumber == numberOfRooms - 1))) {
					bufferedWriter.newLine();
				}
			}
		}

		bufferedWriter.close();

		System.out.print(" ... success.\n");
	}

}
