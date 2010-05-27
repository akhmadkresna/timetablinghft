package de.hft.timetabling.reader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.IRoom;

final class ProblemInstanceImpl implements IProblemInstance {

	private final String name;

	private final int numberOfConstraints;

	private final int numberOfCourses;

	private final int numberOfCurricula;

	private final int numberOfDays;

	private final int numberOfRooms;

	private final int periodsPerDay;

	private final List<ICourse> courses;

	private final List<IRoom> rooms;

	private final List<ICurriculum> curricula;

	private final Map<ICourse, List<Integer>> unavailabilityConstraints;

	public ProblemInstanceImpl(String name, int numberOfCourses,
			int numberOfRooms, int numberOfDays, int periodsPerDay,
			int numberOfCurricula, int numberOfConstraints) {

		this.name = name;
		this.numberOfCourses = numberOfCourses;
		this.numberOfRooms = numberOfRooms;
		this.numberOfDays = numberOfDays;
		this.periodsPerDay = periodsPerDay;
		this.numberOfCurricula = numberOfCurricula;
		this.numberOfConstraints = numberOfConstraints;

		courses = new ArrayList<ICourse>();
		rooms = new ArrayList<IRoom>();
		curricula = new ArrayList<ICurriculum>();
		unavailabilityConstraints = new HashMap<ICourse, List<Integer>>();
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
		List<Integer> periodsForCourse = unavailabilityConstraints.get(course);
		if (periodsForCourse == null) {
			periodsForCourse = new ArrayList<Integer>();
		}
		periodsForCourse.add(period);
		unavailabilityConstraints.put(course, periodsForCourse);
	}

	@Override
	public List<ICourse> getCourses() {
		return Collections.unmodifiableList(courses);
	}

	@Override
	public List<ICurriculum> getCurricula() {
		return Collections.unmodifiableList(curricula);
	}

	@Override
	public List<IRoom> getRooms() {
		return Collections.unmodifiableList(rooms);
	}

	@Override
	public List<Integer> getUnavailabilityConstraints(ICourse course) {
		List<Integer> constraints = unavailabilityConstraints.get(course);
		if (constraints != null) {
			return Collections.unmodifiableList(constraints);
		}
		return new ArrayList<Integer>(0);
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
