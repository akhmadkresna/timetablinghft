package de.hft.timetabling.reader;

import java.util.LinkedHashSet;
import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IProblemInstance;

/**
 * Immutable implementation of a course.
 * 
 * @author Alexander Weickmann
 * 
 * @see ICourse
 */
final class CourseImpl implements ICourse {

	private final String id;

	private final int minWorkingDays;

	private final int numberOfLectures;

	private final int numberOfStudents;

	private final String teacher;

	private final IProblemInstance problemInstance;

	CourseImpl(String id, int minWorkingDays, int numberOfLectures,
			int numberOfStudents, String teacher,
			IProblemInstance problemInstance) {

		this.id = id;
		this.minWorkingDays = minWorkingDays;
		this.numberOfLectures = numberOfLectures;
		this.numberOfStudents = numberOfStudents;
		this.teacher = teacher;
		this.problemInstance = problemInstance;
	}

	@Override
	public IProblemInstance getProblemInstance() {
		return problemInstance;
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
	public Set<ICurriculum> getCurricula() {
		Set<ICurriculum> curricula = new LinkedHashSet<ICurriculum>();
		for (ICurriculum curriculum : problemInstance.getCurricula()) {
			if (curriculum.containsCourse(this)) {
				curricula.add(curriculum);
			}
		}
		return curricula;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((problemInstance == null) ? 0 : problemInstance.hashCode());
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
		if (problemInstance == null) {
			if (other.problemInstance != null) {
				return false;
			}
		} else if (!problemInstance.equals(other.problemInstance)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Course: " + id + " (" + teacher + ")";
	}

}
