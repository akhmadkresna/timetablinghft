package de.hft.timetabling.reader;

import java.io.BufferedReader;
import java.io.DataInputStream;
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
import de.hft.timetabling.services.IReaderService;

/**
 * Implementation of the reader service.
 * 
 * @author Alexander Weickmann
 * 
 * @see IReaderService
 */
public final class Reader implements IReaderService {

	@Override
	public IProblemInstance readInstance(String fileName) throws IOException {
		List<String> lines = readFile(fileName);
		ProblemInstanceImpl instance = parseGeneralInformation(lines);
		parseContents(lines, instance);
		return instance;
	}

	/**
	 * Reads the input file specified by the file name line by line and returns
	 * the contents as a list of strings.
	 */
	private List<String> readFile(String fileName) throws IOException {
		List<String> lines = new ArrayList<String>();
		BufferedReader bufferedReader = getBufferedReader(fileName);
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
	private BufferedReader getBufferedReader(String fileName)
			throws FileNotFoundException {

		FileInputStream fileStream = new FileInputStream(fileName);
		DataInputStream dataStream = new DataInputStream(fileStream);
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(dataStream));
		return bufferedReader;
	}

	/**
	 * Parses the contents of the read file and fills the provided problem
	 * instance with information.
	 */
	private void parseContents(List<String> lines, ProblemInstanceImpl instance) {
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
	private ProblemInstanceImpl parseGeneralInformation(List<String> lines) {
		String name = getGeneralInfoValue(lines.get(0));
		int numberOfCourses = Integer
				.valueOf(getGeneralInfoValue(lines.get(1)));
		int numberOfRooms = Integer.valueOf(getGeneralInfoValue(lines.get(2)));
		int numberOfDays = Integer.valueOf(getGeneralInfoValue(lines.get(3)));
		int periodsPerDay = Integer.valueOf(getGeneralInfoValue(lines.get(4)));
		int numberOfCurricula = Integer.valueOf(getGeneralInfoValue(lines
				.get(5)));
		int numberOfConstraints = Integer.valueOf(getGeneralInfoValue(lines
				.get(6)));

		return new ProblemInstanceImpl(name, numberOfCourses, numberOfRooms,
				numberOfDays, periodsPerDay, numberOfCurricula,
				numberOfConstraints);
	}

	/**
	 * Retrieves the actual value associated with a general information line.
	 */
	private String getGeneralInfoValue(String line) {
		return line.substring(line.lastIndexOf(":") + 2);
	}

	private void parseUnavailabilityConstraint(String line,
			ProblemInstanceImpl instance) {

		StringTokenizer tokenizer = new StringTokenizer(line, " ");
		String courseId = tokenizer.nextToken();
		ICourse course = instance.getCourseById(courseId);

		int day = Integer.valueOf(tokenizer.nextToken());
		int period = Integer.valueOf(tokenizer.nextToken());
		int periodsPerDay = instance.getPeriodsPerDay();
		int convertedPeriod = convertPeriod(day, period, periodsPerDay);

		instance.addUnavailabilityConstraint(course, convertedPeriod);
	}

	private void parseCurriculum(String line, ProblemInstanceImpl instance) {
		StringTokenizer tokenizer = new StringTokenizer(line, " ");
		String id = tokenizer.nextToken();
		int numberOfCourses = Integer.valueOf(tokenizer.nextToken());
		CurriculumImpl curriculum = new CurriculumImpl(id, numberOfCourses);

		while (tokenizer.countTokens() > 0) {
			String courseId = tokenizer.nextToken();
			ICourse memberCourse = instance.getCourseById(courseId);
			curriculum.addCourse(memberCourse);
		}

		instance.addCurriculum(curriculum);
	}

	private void parseRoom(String line, ProblemInstanceImpl instance) {
		StringTokenizer tokenizer = new StringTokenizer(line, " ");
		String id = tokenizer.nextToken();
		int capacity = Integer.valueOf(tokenizer.nextToken());
		IRoom room = new RoomImpl(id, capacity);
		instance.addRoom(room);
	}

	private void parseCourse(String line, ProblemInstanceImpl instance) {
		StringTokenizer tokenizer = new StringTokenizer(line, " ");
		String id = tokenizer.nextToken();
		String teacher = tokenizer.nextToken();
		int numberOfLectures = Integer.valueOf(tokenizer.nextToken());
		int minWorkingDays = Integer.valueOf(tokenizer.nextToken());
		int numberOfStudents = Integer.valueOf(tokenizer.nextToken());
		ICourse course = new CourseImpl(id, minWorkingDays, numberOfLectures,
				numberOfStudents, teacher, instance);
		instance.addCourse(course);
	}

	/**
	 * Converts the day-period format used by the competition's input format to
	 * a period-only format.
	 */
	private int convertPeriod(int day, int period, int periodsPerDay) {
		return period + day * periodsPerDay;
	}

	@Override
	public String toString() {
		return "Reader";
	}

}
