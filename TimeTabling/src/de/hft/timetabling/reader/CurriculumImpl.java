package de.hft.timetabling.reader;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;

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

	CurriculumImpl(String id, int numberOfCourses) {
		this.id = id;
		this.numberOfCourses = numberOfCourses;
		courses = new LinkedHashSet<ICourse>(numberOfCourses);
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
	public boolean containsCourse(ICourse course) {
		return courses.contains(course);
	}

	void addCourse(ICourse course) {
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
		CurriculumImpl other = (CurriculumImpl) obj;
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
