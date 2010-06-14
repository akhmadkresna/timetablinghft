package de.hft.timetabling.reader;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IProblemInstance;

/**
 * Immutable implementation of a curriculum.
 * 
 * @author Alexander Weickmann
 * 
 * @see ICurriculum
 */
final class CurriculumImpl implements ICurriculum {

	private final String id;

	private final int numberOfCourses;

	private final Set<ICourse> courses;

	private final IProblemInstance problemInstance;

	CurriculumImpl(final String id, final int numberOfCourses,
			final IProblemInstance problemInstance) {

		this.id = id;
		this.numberOfCourses = numberOfCourses;
		this.problemInstance = problemInstance;
		courses = new LinkedHashSet<ICourse>(numberOfCourses);
	}

	@Override
	public IProblemInstance getProblemInstance() {
		return problemInstance;
	}

	@Override
	public Set<ICourse> getCourses() {
		return Collections.unmodifiableSet(courses);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public int getNumberOfCourses() {
		return numberOfCourses;
	}

	@Override
	public boolean containsCourse(final ICourse course) {
		return courses.contains(course);
	}

	void addCourse(final ICourse course) {
		courses.add(course);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final CurriculumImpl other = (CurriculumImpl) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Curriculum: " + id + " (" + numberOfCourses + ")";
	}

}
