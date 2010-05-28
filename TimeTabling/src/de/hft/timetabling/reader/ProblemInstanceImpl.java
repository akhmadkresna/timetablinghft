package de.hft.timetabling.reader;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.IRoom;

/**
 * Immutable implementation of the {@link IProblemInstance} interface.
 * 
 * @author Alexander Weickmann
 * 
 * @see IProblemInstance
 */
final class ProblemInstanceImpl implements IProblemInstance {

	private final String fileName;

	private final String name;

	private final int numberOfConstraints;

	private final int numberOfCourses;

	private final int numberOfCurricula;

	private final int numberOfDays;

	private final int numberOfRooms;

	private final int periodsPerDay;

	private final Set<ICourse> courses;

	private final Set<IRoom> rooms;

	private final Set<ICurriculum> curricula;

	private final Map<ICourse, Set<Integer>> unavailabilityConstraints;

	public ProblemInstanceImpl(String fileName, String name,
			int numberOfCourses, int numberOfRooms, int numberOfDays,
			int periodsPerDay, int numberOfCurricula, int numberOfConstraints) {

		this.fileName = fileName;
		this.name = name;
		this.numberOfCourses = numberOfCourses;
		this.numberOfRooms = numberOfRooms;
		this.numberOfDays = numberOfDays;
		this.periodsPerDay = periodsPerDay;
		this.numberOfCurricula = numberOfCurricula;
		this.numberOfConstraints = numberOfConstraints;

		courses = new LinkedHashSet<ICourse>();
		rooms = new LinkedHashSet<IRoom>();
		curricula = new LinkedHashSet<ICurriculum>();
		unavailabilityConstraints = new HashMap<ICourse, Set<Integer>>();
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getNumberOfConstraints() {
		return numberOfConstraints;
	}

	@Override
	public int getNumberOfCourses() {
		return numberOfCourses;
	}

	@Override
	public int getNumberOfCurricula() {
		return numberOfCurricula;
	}

	@Override
	public int getNumberOfDays() {
		return numberOfDays;
	}

	@Override
	public int getNumberOfRooms() {
		return numberOfRooms;
	}

	@Override
	public int getPeriodsPerDay() {
		return periodsPerDay;
	}

	void addCourse(ICourse course) {
		courses.add(course);
	}

	void addRoom(IRoom room) {
		rooms.add(room);
	}

	void addCurriculum(ICurriculum curriculum) {
		curricula.add(curriculum);
	}

	void addUnavailabilityConstraint(ICourse course, int period) {
		Set<Integer> periodsForCourse = unavailabilityConstraints.get(course);
		if (periodsForCourse == null) {
			periodsForCourse = new LinkedHashSet<Integer>();
		}
		periodsForCourse.add(period);
		unavailabilityConstraints.put(course, periodsForCourse);
	}

	@Override
	public Set<ICourse> getCourses() {
		return Collections.unmodifiableSet(courses);
	}

	@Override
	public Set<ICurriculum> getCurricula() {
		return Collections.unmodifiableSet(curricula);
	}

	@Override
	public Set<IRoom> getRooms() {
		return Collections.unmodifiableSet(rooms);
	}

	@Override
	public Set<Integer> getUnavailabilityConstraints(ICourse course) {
		Set<Integer> constraints = unavailabilityConstraints.get(course);
		if (constraints == null) {
			constraints = new LinkedHashSet<Integer>(0);
		}
		return Collections.unmodifiableSet(constraints);
	}

	@Override
	public ICourse getCourseById(String courseId) {
		for (ICourse course : courses) {
			if (course.getId().equals(courseId)) {
				return course;
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((courses == null) ? 0 : courses.hashCode());
		result = prime * result
				+ ((curricula == null) ? 0 : curricula.hashCode());
		result = prime * result
				+ ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + numberOfConstraints;
		result = prime * result + numberOfCourses;
		result = prime * result + numberOfCurricula;
		result = prime * result + numberOfDays;
		result = prime * result + numberOfRooms;
		result = prime * result + periodsPerDay;
		result = prime * result + ((rooms == null) ? 0 : rooms.hashCode());
		result = prime
				* result
				+ ((unavailabilityConstraints == null) ? 0
						: unavailabilityConstraints.hashCode());
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
		ProblemInstanceImpl other = (ProblemInstanceImpl) obj;
		if (courses == null) {
			if (other.courses != null) {
				return false;
			}
		} else if (!courses.equals(other.courses)) {
			return false;
		}
		if (curricula == null) {
			if (other.curricula != null) {
				return false;
			}
		} else if (!curricula.equals(other.curricula)) {
			return false;
		}
		if (fileName == null) {
			if (other.fileName != null) {
				return false;
			}
		} else if (!fileName.equals(other.fileName)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (numberOfConstraints != other.numberOfConstraints) {
			return false;
		}
		if (numberOfCourses != other.numberOfCourses) {
			return false;
		}
		if (numberOfCurricula != other.numberOfCurricula) {
			return false;
		}
		if (numberOfDays != other.numberOfDays) {
			return false;
		}
		if (numberOfRooms != other.numberOfRooms) {
			return false;
		}
		if (periodsPerDay != other.periodsPerDay) {
			return false;
		}
		if (rooms == null) {
			if (other.rooms != null) {
				return false;
			}
		} else if (!rooms.equals(other.rooms)) {
			return false;
		}
		if (unavailabilityConstraints == null) {
			if (other.unavailabilityConstraints != null) {
				return false;
			}
		} else if (!unavailabilityConstraints
				.equals(other.unavailabilityConstraints)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Problem Instance: " + name;
	}

}
