package de.hft.timetabling.main;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.IRoom;
import de.hft.timetabling.reader.Reader;
import de.hft.timetabling.services.IReaderService;
import de.hft.timetabling.services.ServiceLocator;

public final class Main {

	public static void main(String[] args) {
		ServiceLocator.getInstance().setReaderService(new Reader());

		try {
			exampleUsage();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void exampleUsage() throws IOException {
		IReaderService readerService = ServiceLocator.getInstance()
				.getReaderService();
		IProblemInstance problemInstance = readerService
				.readInstance("example.txt");

		System.out.println("GENERAL INFORMATION");
		System.out.println("Name: " + problemInstance.getName());
		System.out.println("Courses: " + problemInstance.getNumberOfCourses());
		System.out.println("Rooms: " + problemInstance.getNumberOfRooms());
		System.out.println("Days: " + problemInstance.getNumberOfDays());
		System.out.println("Periods per day: "
				+ problemInstance.getPeriodsPerDay());
		System.out.println("Curricula: "
				+ problemInstance.getNumberOfCurricula());
		System.out.println("Constraints: "
				+ problemInstance.getNumberOfConstraints());
		System.out.println("");

		System.out.println("COURSES");
		for (ICourse course : problemInstance.getCourses()) {
			System.out.println(course.getId() + " " + course.getTeacher() + " "
					+ course.getNumberOfLectures() + " "
					+ course.getMinWorkingDays() + " "
					+ course.getNumberOfStudents());
		}
		System.out.println("");

		System.out.println("ROOMS");
		for (IRoom room : problemInstance.getRooms()) {
			System.out.println(room.getId() + " " + room.getCapacity());
		}
		System.out.println("");

		System.out.println("CURRICULA");
		for (ICurriculum curriculum : problemInstance.getCurricula()) {
			System.out.print(curriculum.getId() + " "
					+ curriculum.getNumberOfCourses());
			for (ICourse course : curriculum.getCourses()) {
				System.out.print(" " + course.getId());
			}
			System.out.print("\n");
		}
		System.out.println("");

		System.out.println("UNAVAILABILITY CONSTRAINTS");
		Map<ICourse, List<Integer>> unavailabilityConstraints = problemInstance
				.getUnavailabilityConstraints();
		for (ICourse course : unavailabilityConstraints.keySet()) {
			System.out.print(course.getId());
			for (Integer forbiddenPeriod : unavailabilityConstraints
					.get(course)) {
				System.out.print(" " + forbiddenPeriod);
			}
			System.out.print("\n");
		}
	}
}
