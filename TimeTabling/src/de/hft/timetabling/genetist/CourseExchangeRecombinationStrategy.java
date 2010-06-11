package de.hft.timetabling.genetist;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;

/**
 * This recombination strategy performs recombination by taking one half of all
 * courses from one solution and the other half from the other solution.
 * <p>
 * Of course, multiple hard constraint violations can occur during this
 * procedure. The individual lectures are assigned to the new solution one after
 * the other. When placing each assignment, it is checked if this specific
 * assignment will be hard constraint valid. If it is not, the lecture to be
 * assigned will be stored in a separate list containing all lectures yet to be
 * assigned. The position where that course should be originally assigned is
 * also stored. The same will be done, if a course cannot be placed because
 * there is already another assignment belonging to those provided by the other
 * solution.
 * <p>
 * At the end of the recombination an attempt is made to assign all remaining
 * lectures. Starting from the original locations where they should have been
 * assigned but where it was not possible, new locations are computed. This is
 * done by first checking the other rooms in the period. If this fails it will
 * be either placed at a lower period or at a higher period. This depends on the
 * distance to the nearest valid empty slot, which will be computed.
 * <p>
 * If at least one lecture cannot be assigned, <tt>null</tt> will be returned as
 * the result of the recombination. In this case the two solutions can be
 * considered as being not compatible with each other.
 * 
 * @author Alexander Weickmann
 */
public final class CourseExchangeRecombinationStrategy extends
		RecombinationStrategy {

	private IProblemInstance instance;

	/** Set of courses that shall be provided by solution 1. */
	private Set<ICourse> courses1;

	/** Set of courses that shall be provided by solution 2. */
	private Set<ICourse> courses2;

	private ICourse[][] solution1Coding;

	private ICourse[][] solution2Coding;

	/** The coding that is being build during the algorithm. */
	private ICourse[][] childCoding;

	/**
	 * List of lectures that could not be assigned during the first step of the
	 * algorithm.
	 */
	List<Lecture> notAssignedLectures;

	public CourseExchangeRecombinationStrategy() {
		courses1 = new HashSet<ICourse>();
		courses2 = new HashSet<ICourse>();
		notAssignedLectures = new LinkedList<Lecture>();
	}

	@Override
	protected void reset() {
		instance = null;
		courses1.clear();
		courses2.clear();
		solution1Coding = null;
		solution2Coding = null;
		childCoding = null;
		notAssignedLectures.clear();
	}

	@Override
	public ISolution recombine(ISolution solution1, ISolution solution2) {
		instance = solution1.getProblemInstance();
		solution1Coding = solution1.getCoding();
		solution2Coding = solution2.getCoding();

		int nrPeriods = instance.getNumberOfPeriods();
		int nrRooms = instance.getNumberOfRooms();
		childCoding = new ICourse[nrPeriods][nrRooms];

		determineCourseSets();

		performRecombination();

		ISolution childSolution = null;
		boolean success = assignNotAssignedLectures();
		if (success) {
			childSolution = getSolutionTable().createNewSolution(childCoding,
					instance);
		}

		return childSolution;
	}

	/** Determines the course sets that will be provided by each solution. */
	private void determineCourseSets() {
		Set<ICourse> allCourses = instance.getCourses();
		int halfSize = allCourses.size() / 2;
		int i = 0;
		for (ICourse course : allCourses) {
			if (i < halfSize) {
				courses1.add(course);
			} else {
				courses2.add(course);
			}
			i++;
		}
	}

	/** Executes the first step of the recombination. */
	private void performRecombination() {
		for (int period = 0; period < instance.getNumberOfPeriods(); period++) {
			for (int room = 0; room < instance.getNumberOfRooms(); room++) {

				ICourse solution1Course = solution1Coding[period][room];
				ICourse solution2Course = solution2Coding[period][room];

				// 1) No lecture in this slot at all.
				if ((solution1Course == null) && (solution2Course == null)) {
					continue;
				}

				/*
				 * If a course of a solution is not part of the courses assigned
				 * to this solution then we are not interested in that course.
				 */
				if (solution1Course != null) {
					if (!(courses1.contains(solution1Course))) {
						solution1Course = null;
					}
				}
				if (solution2Course != null) {
					if (!(courses2.contains(solution2Course))) {
						solution2Course = null;
					}
				}

				// 2) There are no relevant courses in this slot.
				if ((solution1Course == null) && (solution2Course == null)) {
					continue;
				}

				// 3) Relevant assignment only in solution 1.
				if ((solution1Course != null) && (solution2Course == null)) {
					recombineOne(solution1Course, period, room);
					continue;
				}

				// 4) Relevant assignment only in solution 2.
				if ((solution1Course == null) && (solution2Course != null)) {
					recombineOne(solution2Course, period, room);
					continue;
				}

				// 5) Relevant assignments in both solutions.
				if ((solution1Course != null) && (solution2Course != null)) {
					recombineTwo(solution1Course, solution2Course, period, room);
					continue;
				}
			}
		}
	}

	/**
	 * Handles the situation in which only one solution offers a relevant
	 * assignment.
	 */
	private void recombineOne(ICourse course, int period, int room) {
		boolean success = assign(course, period, room);
		if (!(success)) {
			Lecture lecture = new Lecture(course, period, room);
			notAssignedLectures.add(lecture);
		}
	}

	/**
	 * Handles the situation in which both solutions offer relevant assignments.
	 */
	private void recombineTwo(ICourse solution1Course, ICourse solution2Course,
			int period, int room) {

		ICourse courseToAssign = solution1Course;
		ICourse courseNotAssigned = solution2Course;
		Random random = new Random();
		if (random.nextBoolean()) {
			courseToAssign = solution2Course;
			courseNotAssigned = solution1Course;
		}

		boolean success = assign(courseToAssign, period, room);
		if (success) {
			notAssignedLectures
					.add(new Lecture(courseNotAssigned, period, room));
		} else {
			notAssignedLectures.add(new Lecture(courseToAssign, period, room));
			boolean successOther = assign(courseNotAssigned, period, room);
			if (!(successOther)) {
				notAssignedLectures.add(new Lecture(courseNotAssigned, period,
						room));
			}
		}
	}

	/**
	 * Attempts to assign those lectures that could not be assigned during the
	 * recombination process. If at least one lecture cannot be assigned,
	 * <tt>false</tt> is returned.
	 */
	private boolean assignNotAssignedLectures() {
		for (Lecture lecture : notAssignedLectures) {
			ICourse course = lecture.getCourse();
			Slot slot = lecture.getSlot();

			/*
			 * First try to assign the lecture to another room in the same
			 * period.
			 */
			/*
			 * TODO AW: Improvement - I think it's more important in regard to
			 * soft constraints to have a constant room rather than to have a
			 * constant period.
			 */
			if (assignToFreeValidSlotInPeriod(slot.getPeriod(), course)) {
				continue;
			}

			Slot newSlot = findNearestFreeValidSlot(slot, course);
			if (newSlot == null) {
				return false;
			}
			safeAssign(course, newSlot);
		}
		return true;
	}

	private Slot findNearestFreeValidSlot(Slot baseSlot, ICourse course) {
		// TODO AW: Not yet really finished as for now only one direction is
		// checked (finds a free slot but not necessarily the nearest).
		int startPeriod = baseSlot.getPeriod();
		int nextPeriod = getNextPeriod(startPeriod);
		while (!(nextPeriod == startPeriod)) {
			for (int room = 0; room < instance.getNumberOfRooms(); room++) {
				if (isValidToAssign(course, nextPeriod, room)) {
					return new Slot(nextPeriod, room);
				}
			}
			nextPeriod = getNextPeriod(nextPeriod);
		}
		return null;
	}

	private int getNextPeriod(int basePeriod) {
		return (basePeriod + 1) % instance.getNumberOfPeriods();
	}

	private boolean assignToFreeValidSlotInPeriod(int period, ICourse course) {
		boolean success = false;
		for (int room = 0; room < childCoding[period].length; room++) {
			success = assign(course, period, room);
			if (success) {
				break;
			}
		}
		return success;
	}

	/**
	 * Tries to assign the given course at the given period and room. Returns
	 * <tt>true</tt> if successful. Returns <tt>false</tt> if the assignment was
	 * not possible due to hard constraint violation or because there is already
	 * an assignment.
	 */
	private boolean assign(ICourse course, int period, int room) {
		if (isValidToAssign(course, period, room)) {
			safeAssign(course, new Slot(period, room));
			return true;
		}
		return false;
	}

	/**
	 * This method is based on the assumption that it is called only when it is
	 * safe to assign the given course to the given slot.
	 */
	private void safeAssign(ICourse course, Slot slot) {
		if (course == null) {
			throw new NullPointerException();
		}
		childCoding[slot.getPeriod()][slot.getRoom()] = course;
	}

	/**
	 * Checks if it is possible to assign the given course to the given period
	 * and room without violating any hard constraints or overwriting an
	 * existing assignment.
	 */
	private boolean isValidToAssign(ICourse course, int period, int room) {
		if (childCoding[period][room] != null) {
			return false;
		}
		boolean teacherViolated = existsTeacherInPeriod(childCoding, course
				.getTeacher(), period);
		boolean curriculaViolated = existsCurriculaInPeriod(childCoding, course
				.getCurricula(), period);
		boolean unavailabilityConstraintViolated = existsUnavailabilityConstraint(
				course, period);
		return !(teacherViolated || unavailabilityConstraintViolated || curriculaViolated);
	}

	@Override
	public String getName() {
		return "Course Exchange v1";
	}

	private static class Slot {

		private final int period;

		private final int room;

		public Slot(int period, int room) {
			this.period = period;
			this.room = room;
		}

		public int getPeriod() {
			return period;
		}

		public int getRoom() {
			return room;
		}

		@Override
		public String toString() {
			return "Period: " + period + ", Room: " + room;
		}

	}

	private static class Lecture {

		private final ICourse course;

		private final Slot slot;

		public Lecture(ICourse course, int period, int room) {
			this.course = course;
			slot = new Slot(period, room);
		}

		public ICourse getCourse() {
			return course;
		}

		public Slot getSlot() {
			return slot;
		}

	}

}
