package de.hft.timetabling.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.IRoom;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.main.Main;
import de.hft.timetabling.services.ICrazyGenetistService;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.IWriterService;
import de.hft.timetabling.services.ServiceLocator;
import de.hft.timetabling.util.DateUtil;
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
		final ISolutionTableService solutionTableService = ServiceLocator
				.getInstance().getSolutionTableService();
		final ISolution bestSolution = solutionTableService
				.getBestPenaltySolution();
		outputSolution(bestSolution);
	}

	@Override
	public void outputSolution(final ISolution solution) throws IOException {
		final IProblemInstance problemInstance = solution.getProblemInstance();
		String fileName = problemInstance.getFileName();
		fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
		fileName = fileName.substring(0, fileName.lastIndexOf('.'));
		fileName = "output/" + fileName + "_"
				+ DateUtil.getTimeStamp(new Date()) + ".ctt";
		outputSolution(fileName, solution);
	}

	@Override
	public void outputSolution(final String fileName, final ISolution solution)
			throws IOException {

		System.out.print("WRITER: Writing solution to '" + fileName + "'");

		final FileWriter fileWriter = new FileWriter(fileName);
		final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		final IProblemInstance problemInstance = solution.getProblemInstance();
		final ICourse[][] coding = solution.getCoding();
		final int numberOfPeriods = problemInstance.getNumberOfPeriods();
		final int numberOfRooms = problemInstance.getNumberOfRooms();
		for (int period = 0; period < numberOfPeriods; period++) {
			final ICourse[] coursesInPeriod = coding[period];
			for (int roomNumber = 0; roomNumber < numberOfRooms; roomNumber++) {

				final ICourse course = coursesInPeriod[roomNumber];
				// Continue if no assignment at this location.
				if (course == null) {
					continue;
				}

				final IRoom room = problemInstance
						.getRoomByUniqueNumber(roomNumber);
				final int periodsPerDay = problemInstance.getPeriodsPerDay();
				final int day = PeriodUtil.getDayFromPeriodOnly(period,
						periodsPerDay);
				final int convertedPeriod = PeriodUtil.convertToDayPeriod(
						period, periodsPerDay);

				bufferedWriter.write(course.getId() + " " + room.getId() + " "
						+ day + " " + convertedPeriod);
				// Append new line if not at end of file.
				if (!((period == numberOfPeriods - 1) && (roomNumber == numberOfRooms - 1))) {
					bufferedWriter.newLine();
				}
			}
		}

		bufferedWriter.close();

		outputHtmlFile(fileName, solution, problemInstance);

		System.out.print(" ... success.\n");
	}

	private void outputHtmlFile(String fileName, final ISolution solution,
			final IProblemInstance problemInstance) throws IOException {

		fileName = getHtmlFileName(fileName);
		final int lastSlashPos = fileName.lastIndexOf('/');
		final String htmlPathName = getHtmlDirectoryPathName(fileName,
				lastSlashPos);
		createHtmlDirectoryIfNonExistent(htmlPathName);
		fileName = htmlPathName + "/" + fileName.substring(lastSlashPos + 1);
		final FileWriter fileWriter = new FileWriter(fileName);
		final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		writeHtmlHeader(fileName, bufferedWriter);

		writeln(bufferedWriter, "<body>");

		writeStatistics(solution, problemInstance, bufferedWriter);

		// Write one table for each curriculum
		for (final ICurriculum curriculum : problemInstance.getCurricula()) {

			writeln(bufferedWriter, "<h2>" + curriculum.getId() + "</h2>");
			writeln(bufferedWriter,
					"<table width=\"100%\" style=\"border: 1px solid black;\">");

			// Write table header
			bufferedWriter.write("<tr>");
			bufferedWriter.newLine();
			for (int day = 0; day < problemInstance.getNumberOfDays(); day++) {
				writeln(bufferedWriter,
						"<th style=\"border: 1px solid black; padding:2px;\">Day "
								+ (day + 1) + "</th>");
			}
			writeln(bufferedWriter, "</tr>");

			for (int period = 0; period < problemInstance.getPeriodsPerDay(); period++) {
				bufferedWriter.write("<tr>");
				bufferedWriter.newLine();

				for (int day = 0; day < problemInstance.getNumberOfDays(); day++) {
					String courseString = "&nbsp;";
					String roomString = "";
					final int convertedPeriod = PeriodUtil.convertToPeriodOnly(
							day, period, problemInstance.getPeriodsPerDay());
					for (int room = 0; room < problemInstance
							.getNumberOfRooms(); room++) {
						final ICourse course = solution.getCoding()[convertedPeriod][room];
						if (course != null) {
							if (course.getCurricula().contains(curriculum)) {
								courseString = course.getId();
								final IRoom roomObj = problemInstance
										.getRoomByUniqueNumber(room);
								roomString = "<br /><em>- " + roomObj.getId()
										+ " [" + course.getNumberOfStudents()
										+ " / " + roomObj.getCapacity()
										+ "] -</em>";
								break;
							}
						}
					}

					bufferedWriter
							.write("<td style=\"border: 1px solid black; text-align:center; padding:2px;\">");
					bufferedWriter.write(courseString + roomString);

					writeln(bufferedWriter, "</td>");
				}

				writeln(bufferedWriter, "</tr>");
			}

			writeln(bufferedWriter, "</table><br />&nbsp;");

		}

		writeln(bufferedWriter, "</body>");

		writeHtmlFooter(bufferedWriter);

		bufferedWriter.close();
	}

	private void writeStatistics(final ISolution solution,
			final IProblemInstance problemInstance,
			final BufferedWriter bufferedWriter) throws IOException {

		writeln(bufferedWriter, "<h1>" + problemInstance.getName() + "("
				+ problemInstance.getFileName() + ")</h1>");
		writeln(bufferedWriter, "<h2>General Information and Statistics</h2>");
		writeln(bufferedWriter,
				"<table width=\"620\" style=\"border: 1px solid black;\">");
		writeln(bufferedWriter, "<tr><td>Created:</td><td>"
				+ new Date().toString() + "</td></tr>");
		writeln(bufferedWriter, "<tr><td>Iterations:</td><td>"
				+ Main.iterations + "</td></tr>");
		writeln(bufferedWriter, "<tr><td>Solution Table Size:</td><td>"
				+ getSolutionTable().getMaximumSize() + "</td></tr>");
		writeln(bufferedWriter, "<tr><td>Duration:</td><td>"
				+ DateUtil.toTimeString(Main.duration) + "</td></tr>");
		writeln(bufferedWriter, "<tr><td>Recombination Strategy:</td><td>"
				+ ICrazyGenetistService.RECOMBINATION_STRATEGY.getName()
				+ "</td></tr>");
		final String directory = (Main.initialSolutionDirectory.length() == 0) ? "None"
				: Main.initialSolutionDirectory;
		writeln(bufferedWriter, "<tr><td>Initial Solution Directory:</td><td>"
				+ directory + "</td></tr>");
		writeln(bufferedWriter, "<tr><td>&nbsp;</td></tr>");

		writeln(bufferedWriter, "<tr><td>Solution Penalty:</td><td>"
				+ solution.getPenalty() + "</td></tr>");
		writeln(bufferedWriter, "<tr><td>Solution Fairness:</td><td>"
				+ solution.getFairness() + "</td></tr>");
		writeln(bufferedWriter, "<tr><td>Solution Age:</td><td>"
				+ solution.getAge() + "</td></tr>");
		writeln(bufferedWriter,
				"<tr><td>Solution Recombination Count:</td><td>"
						+ solution.getRecombinationCount() + "</td></tr>");
		writeln(bufferedWriter, "<tr><td>&nbsp;</td></tr>");

		writeln(bufferedWriter,
				"<tr><td>Generator (Success / Failure):</td><td>"
						+ Main.generatorSuccess + " / " + Main.generatorFailure
						+ " (" + Main.getGeneratorSuccessRatio()
						+ "%)</td></tr>");
		writeln(bufferedWriter,
				"<tr><td>Recombination (Success / Failure):</td><td>"
						+ Main.recombinationSuccess + " / "
						+ Main.recombinationFailure + " ("
						+ Main.getRecombinationSuccessRatio() + "%)</td></tr>");
		writeln(bufferedWriter,
				"<tr><td>Mutation (Success / Failure):</td><td>"
						+ Main.mutationSuccess + " / " + Main.mutationFailure
						+ " (" + Main.getMutationSuccessRatio()
						+ "%)</td></tr>");
		writeln(bufferedWriter,
				"<tr><td>Solution Table Insertion (Success / Failure):</td><td>"
						+ Main.solutionTableInsertionSuccess + " / "
						+ Main.solutionTableInsertionFailure + " ("
						+ Main.getSolutionTableInsertionSuccessRatio()
						+ "%)</td></tr>");
		writeln(bufferedWriter, "</table>");
		writeln(bufferedWriter, "<br /><hr size=\"1\" />");
	}

	private void createHtmlDirectoryIfNonExistent(final String htmlPathName) {
		final File htmlDirectory = new File(htmlPathName);
		if (!(htmlDirectory.exists())) {
			htmlDirectory.mkdir();
		}
	}

	private String getHtmlDirectoryPathName(final String fileName,
			final int lastSlashPos) {
		String pathName = "";
		if (lastSlashPos != -1) {
			pathName = fileName.substring(0, lastSlashPos);
		}
		pathName = pathName + "/html";
		return pathName;
	}

	private String getHtmlFileName(String fileName) {
		fileName = fileName.substring(0, fileName.lastIndexOf('.'));
		fileName = fileName + ".html";
		return fileName;
	}

	private void writeHtmlFooter(final BufferedWriter bufferedWriter)
			throws IOException {

		bufferedWriter.write("</html>");
	}

	private void writeHtmlHeader(final String fileName,
			final BufferedWriter bufferedWriter) throws IOException {

		writeln(bufferedWriter, "<html>");
		writeln(bufferedWriter, "<head>");
		writeln(bufferedWriter, "<title>" + fileName + "</title>");
		writeln(bufferedWriter, "</head>");
		bufferedWriter.newLine();
	}

	private void writeln(final BufferedWriter bufferedWriter, final String text)
			throws IOException {

		bufferedWriter.write(text);
		bufferedWriter.newLine();
	}

	private ISolutionTableService getSolutionTable() {
		return ServiceLocator.getInstance().getSolutionTableService();
	}

}
