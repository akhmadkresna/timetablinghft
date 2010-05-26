package de.hft.timetabling.reader;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.IRoom;
import de.hft.timetabling.services.IReaderService;

public final class Reader implements IReaderService {

	@Override
	public IProblemInstance readInstance(String fileName) throws IOException {
		ProblemInstanceImpl instance = new ProblemInstanceImpl();
		readFile(fileName, instance);
		return instance;
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

	private void readFile(String fileName, ProblemInstanceImpl instance)
			throws IOException {

		boolean readCourses = false;
		boolean readRooms = false;
		boolean readCurricula = false;
		boolean readUnavailabilityConstraints = false;

		BufferedReader bufferedReader = getBufferedReader(fileName);
		String line = bufferedReader.readLine();
		for (int i = 0; line != null; i++) {
			if (i <= 6) {
				readGeneralInformation(line, i, instance);
			} else {
				if ((line.length() > 0) && !(line.equals("END."))) {
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
							readUnavailabilityConstraints(line, instance);
						}
					}
				}
			}
			line = bufferedReader.readLine();
		}
		bufferedReader.close();
	}

	private void readUnavailabilityConstraints(String line,
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
				numberOfStudents, teacher);
		instance.addCourse(course);
	}

	private void readGeneralInformation(String line, int lineNumber,
			ProblemInstanceImpl instance) {

		String value = line.substring(line.lastIndexOf(":") + 2);
		switch (lineNumber) {
		case 0:
			instance.setName(value);
			break;
		case 1:
			instance.setNumberOfCourses(Integer.valueOf(value));
			break;
		case 2:
			instance.setNumberOfRooms(Integer.valueOf(value));
			break;
		case 3:
			instance.setNumberOfDays(Integer.valueOf(value));
			break;
		case 4:
			instance.setPeriodsPerDay(Integer.valueOf(value));
			break;
		case 5:
			instance.setNumberOfCurricula(Integer.valueOf(value));
			break;
		case 6:
			instance.setNumberOfConstraints(Integer.valueOf(value));
			break;
		}
	}

	@Override
	public String toString() {
		return "Reader";
	}

}
