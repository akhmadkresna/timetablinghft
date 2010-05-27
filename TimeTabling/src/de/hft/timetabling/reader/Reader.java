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

public final class Reader implements IReaderService {

	@Override
	public IProblemInstance readInstance(String fileName) throws IOException {
		List<String> lines = readFile(fileName);
		ProblemInstanceImpl instance = readGeneralInformation(lines);
		readContents(lines, instance);
		return instance;
	}

	private void readContents(List<String> lines, ProblemInstanceImpl instance) {
		boolean readCourses = false;
		boolean readRooms = false;
		boolean readCurricula = false;
		boolean readUnavailabilityConstraints = false;

		for (int i = 8; i < lines.size() - 2; i++) {
			String line = lines.get(i);
			if ((line.length() > 0)) {
				if (line.equals("COURSES:")) {
					readCourses = true;
				} else if (line.equals("ROOMS:")) {
					readCourses = false;
					readRooms = true;
				} else if (line.equals("CURRICULA:")) {
					readRooms = false;
					readCurricula = true;
				} else if (line.equals("UNAVAILABILITY_CONSTRAINTS:")) {
					readCurricula = false;
					readUnavailabilityConstraints = true;
				} else {
					if (readCourses) {
						readCourse(line, instance);
					}
					if (readRooms) {
						readRoom(line, instance);
					}
					if (readCurricula) {
						readCurriculum(line, instance);
					}
					if (readUnavailabilityConstraints) {
						readUnavailabilityConstraint(line, instance);
					}
				}
			}
		}
	}

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

	private BufferedReader getBufferedReader(String fileName)
			throws FileNotFoundException {

		FileInputStream fileStream = new FileInputStream("instances/"
				+ fileName);
		DataInputStream dataStream = new DataInputStream(fileStream);
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(dataStream));
		return bufferedReader;
	}

	private ProblemInstanceImpl readGeneralInformation(List<String> lines) {
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

	private String getGeneralInfoValue(String line) {
		return line.substring(line.lastIndexOf(":") + 2);
	}

	private void readUnavailabilityConstraint(String line,
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

	private int convertPeriod(int day, int period, int periodsPerDay) {
		return period + day * periodsPerDay;
	}

	private void readCurriculum(String line, ProblemInstanceImpl instance) {
		StringTokenizer tokenizer = new StringTokenizer(line, " ");
		String id = tokenizer.nextToken();
		int numberOfCourses = Integer.valueOf(tokenizer.nextToken());
		CurriculumImpl curriculum = new CurriculumImpl(id, numberOfCourses);

		for (int i = 0; i <= tokenizer.countTokens() + 1; i++) {
			String courseId = tokenizer.nextToken();
			ICourse memberCourse = instance.getCourseById(courseId);
			curriculum.addCourse(memberCourse);
		}

		instance.addCurriculum(curriculum);
	}

	private void readRoom(String line, ProblemInstanceImpl instance) {
		StringTokenizer tokenizer = new StringTokenizer(line, " ");
		String id = tokenizer.nextToken();
		int capacity = Integer.valueOf(tokenizer.nextToken());
		IRoom room = new RoomImpl(id, capacity);
		instance.addRoom(room);
	}

	private void readCourse(String line, ProblemInstanceImpl instance) {
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

	@Override
	public String toString() {
		return "Reader";
	}

}
