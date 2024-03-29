package de.hft.timetabling.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IProblemInstance;

public class SlowAssignmentAlgorithm implements GeneratorAlgorithm {

	private final IProblemInstance instance;

	private final Map<ICourse, List<Integer>> availableSlots = new HashMap<ICourse, List<Integer>>();

	private final Map<ICourse, Integer> availablePeriodsCount = new HashMap<ICourse, Integer>();

	private final List<Set<ICurriculum>> curriculaInPeriod = new ArrayList<Set<ICurriculum>>();

	private final List<Set<String>> teachersInPeriod = new ArrayList<Set<String>>();

	private final ICourse[] schedule;

	private boolean calculateSlots = true;

	private final List<ICourse> priorityList = new ArrayList<ICourse>();

	public SlowAssignmentAlgorithm(final IProblemInstance instance) {

		this.instance = instance;
		final int slots = instance.getNumberOfPeriods()
				* instance.getNumberOfRooms();
		schedule = new ICourse[slots];

		for (int i = 0; i < instance.getNumberOfPeriods(); i++) {
			curriculaInPeriod.add(new HashSet<ICurriculum>());
			teachersInPeriod.add(new HashSet<String>());
		}
	}

	/**
	 * This method takes a subset of unassigned courses and calculates the
	 * course which has the least amount of viable slots which it can be
	 * assigned to without violating any hard constraints. This course is called
	 * the most critical course. Note that there can be more than one courses
	 * which are critical, i.e. which have the same amount of available slots.
	 * 
	 * @param courses
	 *            the (sub)set of unassigned courses
	 * @return the course with the least available viable slots in the current
	 *         schedule
	 */
	public ICourse getMostCriticalEvent(final Set<ICourse> courses) {

		/*
		 * recalculate available slots for each course in the given set if
		 * necessary
		 */
		if (calculateSlots) {
			calculateSlots(courses);
		}

		final ICourse critical = priorityList.get(0);
		priorityList.remove(0);

		if (priorityList.isEmpty()) {
			calculateSlots = true;
		}

		return critical;
	}

	/**
	 * This method assigns random slots in the schedule to the given course with
	 * respect to the minimal number of lectures for the course. This method
	 * does not check whether there are sufficiently free periods available.
	 * 
	 * @param course
	 *            the course which will be assigned to slots
	 */
	public void assignRandomViableSlots(final ICourse course) {

		for (int i = 0; i < course.getNumberOfLectures(); i++) {

			/*
			 * randomize the order of the available viable slots
			 */
			final List<Integer> slots = availableSlots.get(course);
			java.util.Collections.shuffle(slots);
			final int randomSlot = slots.get(0);
			schedule[randomSlot] = course;

			/*
			 * keep track of which curricula and teachers are present in the
			 * period the course has been assigned to
			 */
			final int period = getPeriodForSlot(randomSlot);
			curriculaInPeriod.get(period).addAll(course.getCurricula());
			teachersInPeriod.get(period).add(course.getTeacher());

			/*
			 * several viable slots for the given course can be in the same
			 * period. After assignment the slots in the same periods have to be
			 * removed as otherwise hard constraints would be violated on
			 * assignment
			 */
			final List<Integer> remainingSlots = new ArrayList<Integer>();

			for (final int slot : slots) {
				if (period != getPeriodForSlot(slot)) {
					remainingSlots.add(slot);
				}
			}

			availableSlots.put(course, remainingSlots);
		}

		/*
		 * changes to the schedule were done so the data for available slots is
		 * stale now and requires recalculation
		 */
		calculateSlots = true;
	}

	/**
	 * This method returns the two dimensional array representing the coding for
	 * the assignment of courses to periods and rooms. It does *not* guarantee
	 * that the solution is viable in any way.
	 * 
	 * @return the coding of the current schedule
	 */
	public ICourse[][] getCoding() {

		final ICourse[][] coding = new ICourse[instance.getNumberOfPeriods()][instance
				.getNumberOfRooms()];
		int x = 0;

		for (int i = 0; i < instance.getNumberOfPeriods(); i++) {

			for (int j = 0; j < instance.getNumberOfRooms(); j++) {
				coding[i][j] = schedule[x];
				x++;
			}
		}

		return coding;
	}

	/**
	 * This method returns the number of periods to which a course can be
	 * assigned to.
	 * 
	 * @param course
	 *            the course for which the available periods are queried
	 * @return the number of available periods for the given course
	 */
	public int getAvailablePeriodsCount(final ICourse course) {
		return availablePeriodsCount.get(course);
	}

	/**
	 * This method calculates for each course in the given set to which slots
	 * the course can be potentially assigned to without violating hard
	 * constraints. It also keeps track how many viable periods the set of slots
	 * represent.
	 * 
	 * @param courses
	 *            the set of courses for which the viable slots are calculated
	 */
	private void calculateSlots(final Set<ICourse> courses) {

		reset(courses);

		for (final ICourse course : courses) {

			int slot = 0;

			while (slot < schedule.length) {
				final int period = getPeriodForSlot(slot);

				if (periodInvalid(course, period)) {
					slot += instance.getNumberOfRooms();
				} else {
					final int periodEnd = slot + instance.getNumberOfRooms();

					while (slot < periodEnd) {
						if (schedule[slot] == null) {
							availableSlots.get(course).add(slot);
						}

						slot++;
					}

					int periodCount = availablePeriodsCount.get(course);
					periodCount++;
					availablePeriodsCount.put(course, periodCount);
				}
			}
		}

		addMissingCourses(courses);
		sortPriorityList();

		calculateSlots = false;
	}

	/**
	 * This method calculates the period from the given slot number.
	 * 
	 * @param slot
	 *            the slot number for which the period will be calculated
	 * @return the period of the given slot
	 */
	private int getPeriodForSlot(final int slot) {
		return slot / instance.getNumberOfRooms();
	}

	/**
	 * This method checks whether assigning a course to a specific period is
	 * possible with respect to hard constraints and room availability.
	 * 
	 * @param course
	 *            the course to be checked
	 * @param period
	 *            the period in which the course is to be assigned
	 * @return true if and only if any hard constraints are violated or no rooms
	 *         are available in the period, false otherwise
	 */
	private boolean periodInvalid(final ICourse course, final int period) {

		/*
		 * calculate whether curricula in this period are present to which the
		 * given course belongs to by getting the intersection between the set
		 * of curricula the course belongs to with the set of curricula already
		 * present in the period
		 */

		return intersects(course, period)
				|| violatesUnavailabilityConstraints(course, period)
				|| teacherOverlap(course, period) || allRoomsOccupied(period);
	}

	/**
	 * This method checks whether there is already another course in the period
	 * which belongs to the same curriculum.
	 * 
	 * @param course
	 *            the course to be checked
	 * @param period
	 *            the period in which the course is to be assigned
	 * @return true if and only if assigning the course would violate curricula
	 *         hard constraints, false otherwise
	 */
	private boolean intersects(final ICourse course, final int period) {

		final Set<ICurriculum> curricula = curriculaInPeriod.get(period);
		for (final ICurriculum c : course.getCurricula()) {
			if (curricula.contains(c)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method checks whether the teacher giving the specific course has
	 * already been assigned in this period.
	 * 
	 * @param course
	 *            the course to be checked
	 * @param period
	 *            the period in which the course is to be assigned
	 * @return true if and only if the teacher is already busy in this period,
	 *         false otherwise
	 */
	private boolean teacherOverlap(final ICourse course, final int period) {
		return teachersInPeriod.get(period).contains(course.getTeacher());
	}

	/**
	 * This method checks whether unavailability constraints are violated by
	 * assigning the given course to the given period.
	 * 
	 * @param course
	 *            the course to be assigned
	 * @param period
	 *            the period to which the given course will be assigned to
	 * @return true if and only if no unavailability constraints are violated,
	 *         false otherwise
	 */
	private boolean violatesUnavailabilityConstraints(final ICourse course,
			final int period) {
		return instance.getUnavailabilityConstraints(course).contains(period);
	}

	/**
	 * This methods checks whether all rooms are already occupied in the givne
	 * period.
	 * 
	 * @param period
	 *            the period which will be checked
	 * @return true if and only if all rooms are occupied in the given period,
	 *         false otherwise
	 */
	private boolean allRoomsOccupied(final int period) {

		/*
		 * calculate the start and end slots for the given period
		 */
		final int periodStart = period * instance.getNumberOfRooms();
		final int periodEnd = periodStart + instance.getNumberOfRooms();

		for (int i = periodStart; i < periodEnd; i++) {

			if (schedule[i] == null) {
				return false;
			}
		}

		return true;
	}

	/**
	 * This method complements the priority list by all courses for which no
	 * valid slot could be found.
	 * 
	 * @param courses
	 *            the (sub)set of unassigned courses
	 */
	private void addMissingCourses(final Set<ICourse> courses) {

		for (final ICourse course : courses) {

			if (!availablePeriodsCount.keySet().contains(course)) {
				availablePeriodsCount.put(course, 0);
			}
		}
	}

	/**
	 * This method sorts the priority list in accordance to the amount of
	 * available slots. The course with the least amount of available slots is
	 * stored in the first (i.e. 0) position.
	 */
	private void sortPriorityList() {
		// TODO better performing sorting algorithm than Bubblesort

		boolean changed = true;

		while (changed) {

			changed = false;

			for (int i = 0; i < priorityList.size() - 1; i++) {

				final int slotCount_1 = availableSlots.get(priorityList.get(i))
						.size();

				final int slotCount_2 = availableSlots.get(
						priorityList.get(i + 1)).size();

				if (slotCount_1 > slotCount_2) {
					final ICourse course_1 = priorityList.get(i);
					final ICourse course_2 = priorityList.get(i + 1);
					priorityList.set(i, course_2);
					priorityList.set(i + 1, course_1);

					changed = true;
				}
			}
		}
	}

	/**
	 * This method resets the internal memory which is used for calculating
	 * available slots and periods for the given set of courses.
	 * 
	 * @param courses
	 *            the (sub)set of unassigned courses for which available slots
	 *            and periods will be calculated
	 */
	private void reset(final Set<ICourse> courses) {
		priorityList.clear();
		priorityList.addAll(courses);
		availableSlots.clear();
		availablePeriodsCount.clear();

		for (final ICourse course : courses) {
			availableSlots.put(course, new ArrayList<Integer>());
			availablePeriodsCount.put(course, 0);
		}
	}

	@Override
	public boolean isAssignable(final ICourse course) {
		return availablePeriodsCount.get(course) >= course
				.getNumberOfLectures();
	}
}
