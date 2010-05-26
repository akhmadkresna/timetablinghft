package de.hft.timetabling.reader;

import de.hft.timetabling.common.ICourse;

final class CourseImpl implements ICourse {

	private final String id;

	private final int minWorkingDays;

	private final int numberOfLectures;

	private final int numberOfStudents;

	private final String teacher;

	CourseImpl(String id, int minWorkingDays, int numberOfLectures,
			int numberOfStudents, String teacher) {

		this.id = id;
		this.minWorkingDays = minWorkingDays;
		this.numberOfLectures = numberOfLectures;
		this.numberOfStudents = numberOfStudents;
		this.teacher = teacher;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public int getMinWorkingDays() {
		return minWorkingDays;
	}

	@Override
	public int getNumberOfLectures() {
		return numberOfLectures;
	}

	@Override
	public int getNumberOfStudents() {
		return numberOfStudents;
	}

	@Override
	public String getTeacher() {
		return teacher;
	}

	@Override
	public String toString() {
		return "Course: " + id + " (" + teacher + ")";
	}

}
