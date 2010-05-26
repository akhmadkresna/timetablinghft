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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + minWorkingDays;
		result = prime * result + numberOfLectures;
		result = prime * result + numberOfStudents;
		result = prime * result + ((teacher == null) ? 0 : teacher.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CourseImpl other = (CourseImpl) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (minWorkingDays != other.minWorkingDays) {
			return false;
		}
		if (numberOfLectures != other.numberOfLectures) {
			return false;
		}
		if (numberOfStudents != other.numberOfStudents) {
			return false;
		}
		if (teacher == null) {
			if (other.teacher != null) {
				return false;
			}
		} else if (!teacher.equals(other.teacher)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Course: " + id + " (" + teacher + ")";
	}

}
