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
import de.hft.timetabling.services.IGeneratorService;

// TODO Can this class be deleted because it is out dated?
class Generator implements IGeneratorService {

	/*
	 * variable for private use of the implementor
	 */
	private static int instanceCount = 1;

	private boolean assignedCourse = true;
	private boolean solutionFound = false;

	private IProblemInstance instance;

	private Map<ICourse, List<Integer>> availableSlots = new HashMap<ICourse, List<Integer>>();
	private Map<ICourse, Integer> availablePeriods = new HashMap<ICourse, Integer>();

	private List<Set<ICurriculum>> curriculaInPeriod;
	private List<Set<String>> teachersInPeriod;

	private int periods;
	private int slots;

	public ICourse[][] generateFeasibleSolution(IProblemInstance instance)
			throws NoFeasibleSolutionFoundException {
		System.out.println("Instance " + instanceCount++ + ": "
				+ instance.getName());

		this.instance = instance;
		periods = instance.getNumberOfDays() * instance.getPeriodsPerDay();
		slots = periods * instance.getNumberOfRooms();

		resetInternalMemory();

		int maxIter = 25;
		int currentIter = 0;

		ICourse[] schedule = new ICourse[slots];

		while (!solutionFound && (currentIter < maxIter)) {
			resetInternalMemory();
			assignedCourse = true;

			Set<ICourse> prioterized = new HashSet<ICourse>();
			Set<ICourse> nonPrioterized = new HashSet<ICourse>();
			Set<ICourse> unassigned = new HashSet<ICourse>();

			int maxLoops = 10;
			int loops = 0;

			schedule = new ICourse[slots];

			do {
				// System.out.println("Loop: " + loops);

				prioterized.addAll(unassigned);
				unassigned = new HashSet<ICourse>();
				nonPrioterized = new HashSet<ICourse>();
				nonPrioterized.addAll(instance.getCourses());
				nonPrioterized.removeAll(prioterized);

				while (!prioterized.isEmpty()) {
					ICourse critical = getMostCriticalEvent(schedule,
							prioterized);

					if (availablePeriods.get(critical) >= critical
							.getNumberOfLectures()) {
						assignRandomViableSlots(critical, schedule);
						assignedCourse = true;
					} else {
						unassigned.add(critical);
						assignedCourse = false;
						/*
						 * Remove course so it doesn't show up in recalculation
						 * of most critical course
						 */
						availablePeriods.remove(critical);
						availableSlots.remove(critical);
					}
					prioterized.remove(critical);
				}

				while (!nonPrioterized.isEmpty()) {
					ICourse critical = getMostCriticalEvent(schedule,
							nonPrioterized);

					if (availablePeriods.get(critical) >= critical
							.getNumberOfLectures()) {
						assignRandomViableSlots(critical, schedule);
						assignedCourse = true;
					} else {
						unassigned.add(critical);
						assignedCourse = false;
						/*
						 * Remove course so it doesn't show up in recalculation
						 * of most critical course
						 */
						availablePeriods.remove(critical);
						availableSlots.remove(critical);
					}

					nonPrioterized.remove(critical);
				}

				loops++;
			} while (!unassigned.isEmpty() && (loops < maxLoops));

			if (unassigned.isEmpty()) {
				System.out.println("Solution found");
				solutionFound = true;
			} else {
				System.err.println("NO SOLUTION FOUND IN ITERATION");
			}
			currentIter++;
		}

		if (solutionFound) {
			return convert(schedule);
		}

		throw new NoFeasibleSolutionFoundException();
	}

	private ICourse getMostCriticalEvent(ICourse[] schedule,
			Set<ICourse> courses) {

		/*
		 * Only recalculate if changes to the schedule occurred
		 */
		if (assignedCourse) {
			availableSlots = new HashMap<ICourse, List<Integer>>();
			availablePeriods = new HashMap<ICourse, Integer>();

			for (ICourse course : courses) {
				availableSlots.put(course, new ArrayList<Integer>());
				availablePeriods.put(course, 0);
			}

			/*
			 * calculate available slots for each course
			 */
			for (ICourse course : courses) {
				int i = 0;
				while (i < schedule.length) {

					/*
					 * calculate period from slot
					 */
					int period = getPeriodForSlot(i);

					/*
					 * Whole range corresponding to a single period will be
					 * skipped if a) There are already courses from the same
					 * curriculum (any curriculum) in this period or b) the
					 * teacher holding the course is already giving another
					 * lecture in this period or c) unavailability constraints
					 * are violated or d) when all rooms in the period are
					 * already occupied
					 */
					Set<ICurriculum> intersectionCurricula = new HashSet<ICurriculum>();
					intersectionCurricula.addAll(curriculaInPeriod.get(period));
					intersectionCurricula.retainAll(course.getCurricula());

					if ((intersectionCurricula.size() > 0)
							|| teachersInPeriod.get(period).contains(
									course.getTeacher())
							|| violatesConstraints(course, period)
							|| allRoomsOccupied(schedule, period)) {

						/*
						 * skip range belonging to current period
						 */
						i += instance.getNumberOfRooms();
					} else {
						/*
						 * Add available slots
						 */
						int j = i;
						while (i < j + instance.getNumberOfRooms()) {
							/*
							 * Check if the slot is alread reserved
							 */
							if (schedule[i] == null) {
								availableSlots.get(course).add(i);
							}

							i++;
						}

						/*
						 * Remember in how many periods a course can be
						 * potentially placed
						 */
						int p = availablePeriods.get(course);
						p += 1;
						availablePeriods.put(course, p);
					}
				}
			}
		}

		/*
		 * Account for all courses for which no viable slots were found
		 */
		for (ICourse course : courses) {
			if (!availablePeriods.keySet().contains(course)) {
				availablePeriods.put(course, 0);
				availableSlots.put(course, new ArrayList<Integer>());
			}
		}

		/*
		 * Determine the course with the least number of available time slots
		 */
		int minimum = Integer.MAX_VALUE;
		ICourse critical = null;

		for (ICourse course : availableSlots.keySet()) {
			if (availableSlots.get(course).size() < minimum) {
				critical = course;
				minimum = availableSlots.get(course).size();
			}
		}

		return critical;
	}

	private void assignRandomViableSlots(ICourse course, ICourse[] schedule) {
		for (int i = 0; i < course.getNumberOfLectures(); i++) {
			List<Integer> slots = availableSlots.get(course);
			java.util.Collections.shuffle(slots);
			int randomSlot = slots.get(0);
			schedule[randomSlot] = course;
			int period = getPeriodForSlot(randomSlot);
			curriculaInPeriod.get(period).addAll(course.getCurricula());
			teachersInPeriod.get(period).add(course.getTeacher());

			/*
			 * Remove slots from the same period
			 */
			List<Integer> remainingSlots = new ArrayList<Integer>();

			for (int t : slots) {
				if (period != getPeriodForSlot(t)) {
					remainingSlots.add(t);
				}
			}
			availableSlots.put(course, remainingSlots);
		}
	}

	private boolean violatesConstraints(ICourse course, int period) {
		return instance.getUnavailabilityConstraints(course).contains(period);
	}

	private int getPeriodForSlot(int slot) {
		return slot / instance.getNumberOfRooms();
	}

	private void resetInternalMemory() {
		curriculaInPeriod = new ArrayList<Set<ICurriculum>>();
		teachersInPeriod = new ArrayList<Set<String>>();

		for (int i = 0; i < periods; i++) {
			curriculaInPeriod.add(new HashSet<ICurriculum>());
			teachersInPeriod.add(new HashSet<String>());
		}
	}

	private boolean allRoomsOccupied(ICourse[] schedule, int period) {
		int slot = period * instance.getNumberOfRooms();
		int periodEnd = slot + instance.getNumberOfRooms();

		for (int i = slot; i < periodEnd; i++) {
			if (schedule[i] == null) {
				return false;
			}
		}

		return true;
	}

	private ICourse[][] convert(ICourse[] schedule) {
		ICourse[][] finalSchedule = new ICourse[periods][instance
				.getNumberOfRooms()];
		int x = 0;

		for (int i = 0; i < periods; i++) {
			for (int j = 0; j < instance.getNumberOfRooms(); j++) {
				finalSchedule[i][j] = schedule[x];
				x++;
			}
		}

		return finalSchedule;
	}

	@Override
	public void fillSolutionTable(IProblemInstance problemInstance)
			throws NoFeasibleSolutionFoundException {

		// TODO I think this class is out dated and does no longer need to
		// exist?
	}
}
