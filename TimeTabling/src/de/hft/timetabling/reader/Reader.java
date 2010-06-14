package de.hft.timetabling.reader;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.IRoom;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.services.IReaderService;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.ServiceLocator;
import de.hft.timetabling.util.PeriodUtil;

/**
 * Implementation of the reader service.
 * 
 * @author Alexander Weickmann
 * 
 * @see IReaderService
 */
public final class Reader implements IReaderService {

	/** The current unique number that is assigned to a room. */
	private int currentUniqueRoomNumber;

	@Override
	public IProblemInstance readInstance(final String fileName)
			throws IOException {
		System.out.print("READER: Reading input file '" + fileName + "'");
		reset();
		final List<String> lines = readFile(fileName);
		final ProblemInstanceImpl instance = parseGeneralInformation(lines,
				fileName);
		parseContents(lines, instance);
		System.out.print(" ... success.\n");
		return instance;
	}

	@Override
	public IProblemInstance readInstanceUsingInitialSolutionDirectory(
			final String instanceFileName, final String solutionDirectoryName)
			throws IOException {

		final IProblemInstance instance = readInstance(instanceFileName);
		readSolutionDirectory(solutionDirectoryName, instance);
		return instance;
	}

	/**
	 * Reads all solutions stored in the directory identified by the directory
	 * name provided. These solutions are put into the solution table to be used
	 * as initial solutions.
	 */
	private void readSolutionDirectory(final String directoryName,
			final IProblemInstance instance) throws IOException {

		final File folder = new File(directoryName);
		if (!(folder.exists())) {
			throw new FileNotFoundException("The directory '" + directoryName
					+ "' does not exist.");
		}
		if (!(folder.isDirectory())) {
			throw new IOException("The name " + directoryName
					+ " is not actually a directory.");
		}

		System.out.print("READER: Reading solutions from directory '"
				+ directoryName + "' ...");

		final ISolutionTableService solutionTable = ServiceLocator
				.getInstance().getSolutionTableService();

		int nrReadSolutions = 0;
		for (final String fileName : folder.list()) {
			if (!(fileName.endsWith(".ctt"))) {
				continue;
			}
			if (solutionTable.isFull()) {
				break;
			}
			final ISolution solution = readSolution(directoryName + "/"
					+ fileName, instance);
			solutionTable.addSolution(solution);
			nrReadSolutions++;
		}

		System.out.print(" done (" + nrReadSolutions + " initialized).\n");
	}

	/**
	 * Reads one specific solution from an input file following the output
	 * format of the time tabling competition.
	 */
	public ISolution readSolution(final String fileName,
			final IProblemInstance instance) throws IOException {

		final ICourse[][] coding = new ICourse[instance.getNumberOfPeriods()][instance
				.getNumberOfRooms()];

		final BufferedReader bufferedReader = getBufferedReader(fileName);
		String line = bufferedReader.readLine();
		while (line != null) {
			final StringTokenizer tokenizer = new StringTokenizer(line, " ");
			final String courseId = tokenizer.nextToken();
			final String roomId = tokenizer.nextToken();

			final int day = Integer.valueOf(tokenizer.nextToken());
			int period = Integer.valueOf(tokenizer.nextToken());
			period = PeriodUtil.convertToPeriodOnly(day, period, instance
					.getPeriodsPerDay());

			final int roomNr = instance.getRoomById(roomId).getUniqueNumber();
			coding[period][roomNr] = instance.getCourseById(courseId);

			line = bufferedReader.readLine();
		}
		bufferedReader.close();

		final ISolutionTableService solutionTable = ServiceLocator
				.getInstance().getSolutionTableService();
		return solutionTable.createNewSolution(coding, instance);
	}

	private void reset() {
		currentUniqueRoomNumber = 0;
	}

	/**
	 * Reads the input file specified by the file name line by line and returns
	 * the contents as a list of strings.
	 */
	private List<String> readFile(final String fileName) throws IOException {
		final List<String> lines = new ArrayList<String>();
		final BufferedReader bufferedReader = getBufferedReader(fileName);
		String line = bufferedReader.readLine();
		while (line != null) {
			lines.add(line);
			line = bufferedReader.readLine();
		}
		bufferedReader.close();
		return lines;
	}

	/**
	 * Returns a {@link BufferedReader} that can be used to read the file
	 * identified by the given file name.
	 */
	private BufferedReader getBufferedReader(final String fileName)
			throws FileNotFoundException {

		final FileInputStream fileStream = new FileInputStream(fileName);
		final DataInputStream dataStream = new DataInputStream(fileStream);
		final BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(dataStream));
		return bufferedReader;
	}

	/**
	 * Parses the contents of the read file and fills the provided problem
	 * instance with information.
	 */
	private void parseContents(final List<String> lines,
			final ProblemInstanceImpl instance) {
		boolean parseCourses = false;
		boolean parseRooms = false;
		boolean parseCurricula = false;
		boolean parseUnavailabilityConstraints = false;

		for (int i = 8; i < lines.size() - 2; i++) {
			String line = lines.get(i);
			// Replace all tabs with spaces.
			line = line.replaceAll("\t", " ");
			if ((line.length() > 0)) {
				if (line.equals("COURSES:")) {
					parseCourses = true;
				} else if (line.equals("ROOMS:")) {
					parseCourses = false;
					parseRooms = true;
				} else if (line.equals("CURRICULA:")) {
					parseRooms = false;
					parseCurricula = true;
				} else if (line.equals("UNAVAILABILITY_CONSTRAINTS:")) {
					parseCurricula = false;
					parseUnavailabilityConstraints = true;
				} else {
					if (parseCourses) {
						parseCourse(line, instance);
					}
					if (parseRooms) {
						parseRoom(line, instance);
					}
					if (parseCurricula) {
						parseCurriculum(line, instance);
					}
					if (parseUnavailabilityConstraints) {
						parseUnavailabilityConstraint(line, instance);
					}
				}
			}
		}
	}

	/**
	 * Parses the general information lines at the beginning of the file and
	 * creates and returns a new {@link IProblemInstance} based upon this data.
	 */
	private ProblemInstanceImpl parseGeneralInformation(
			final List<String> lines, final String fileName) {

		final String name = getGeneralInfoValue(lines.get(0));
		final int numberOfCourses = Integer.valueOf(getGeneralInfoValue(lines
				.get(1)));
		final int numberOfRooms = Integer.valueOf(getGeneralInfoValue(lines
				.get(2)));
		final int numberOfDays = Integer.valueOf(getGeneralInfoValue(lines
				.get(3)));
		final int periodsPerDay = Integer.valueOf(getGeneralInfoValue(lines
				.get(4)));
		final int numberOfCurricula = Integer.valueOf(getGeneralInfoValue(lines
				.get(5)));
		final int numberOfConstraints = Integer
				.valueOf(getGeneralInfoValue(lines.get(6)));

		return new ProblemInstanceImpl(fileName, name, numberOfCourses,
				numberOfRooms, numberOfDays, periodsPerDay, numberOfCurricula,
				numberOfConstraints);
	}

	/**
	 * Retrieves the actual value associated with a general information line.
	 */
	private String getGeneralInfoValue(final String line) {
		return line.substring(line.lastIndexOf(":") + 2);
	}

	private void parseUnavailabilityConstraint(final String line,
			final ProblemInstanceImpl instance) {

		final StringTokenizer tokenizer = new StringTokenizer(line, " ");
		final String courseId = tokenizer.nextToken();
		final ICourse course = instance.getCourseById(courseId);

		final int day = Integer.valueOf(tokenizer.nextToken());
		final int period = Integer.valueOf(tokenizer.nextToken());
		final int periodsPerDay = instance.getPeriodsPerDay();
		final int convertedPeriod = PeriodUtil.convertToPeriodOnly(day, period,
				periodsPerDay);

		instance.addUnavailabilityConstraint(course, convertedPeriod);
	}

	private void parseCurriculum(final String line,
			final ProblemInstanceImpl instance) {
		final StringTokenizer tokenizer = new StringTokenizer(line, " ");
		final String id = tokenizer.nextToken();
		final int numberOfCourses = Integer.valueOf(tokenizer.nextToken());
		final CurriculumImpl curriculum = new CurriculumImpl(id,
				numberOfCourses, instance);

		while (tokenizer.countTokens() > 0) {
			final String courseId = tokenizer.nextToken();
			final ICourse memberCourse = instance.getCourseById(courseId);
			curriculum.addCourse(memberCourse);
		}

		instance.addCurriculum(curriculum);
	}

	private void parseRoom(final String line, final ProblemInstanceImpl instance) {
		final StringTokenizer tokenizer = new StringTokenizer(line, " ");
		final String id = tokenizer.nextToken();
		final int capacity = Integer.valueOf(tokenizer.nextToken());
		final IRoom room = new RoomImpl(id, capacity, currentUniqueRoomNumber,
				instance);
		instance.addRoom(room);
		currentUniqueRoomNumber++;
	}

	private void parseCourse(final String line,
			final ProblemInstanceImpl instance) {
		final StringTokenizer tokenizer = new StringTokenizer(line, " ");
		final String id = tokenizer.nextToken();
		final String teacher = tokenizer.nextToken();
		final int numberOfLectures = Integer.valueOf(tokenizer.nextToken());
		final int minWorkingDays = Integer.valueOf(tokenizer.nextToken());
		final int numberOfStudents = Integer.valueOf(tokenizer.nextToken());
		final ICourse course = new CourseImpl(id, minWorkingDays,
				numberOfLectures, numberOfStudents, teacher, instance);
		instance.addCourse(course);
	}

	@Override
	public String toString() {
		return "Reader";
	}

}
