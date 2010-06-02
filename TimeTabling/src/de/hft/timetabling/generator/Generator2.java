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

public final class Generator2 implements IGeneratorService {

	private final int MAX_ITERATIONS = 20;

	private final int MAX_LOOPS = 10;

	public ICourse[][] generateFeasibleSolution(final IProblemInstance instance)
			throws NoFeasibleSolutionFoundException {

		int iterations = 0;

		while (iterations < MAX_ITERATIONS) {

			int loops = 0;

			SessionObject session = new SessionObject(instance);

			Set<ICourse> prioterized = new HashSet<ICourse>();
			Set<ICourse> nonPrioterized = new HashSet<ICourse>();
			Set<ICourse> unassigned = new HashSet<ICourse>();

			do {
				prioterized.addAll(unassigned);
				unassigned.clear();
				nonPrioterized.clear();
				nonPrioterized.addAll(instance.getCourses());
				nonPrioterized.removeAll(prioterized);

				unassigned.addAll(assignCourses(session, prioterized));
				unassigned.addAll(assignCourses(session, nonPrioterized));

			} while (!unassigned.isEmpty() && (loops++ < MAX_LOOPS));

			if (unassigned.isEmpty()) {
				return session.getCoding();
			}

			iterations++;
		}

		throw new NoFeasibleSolutionFoundException();
	}

	private Set<ICourse> assignCourses(final SessionObject session,
			final Set<ICourse> courses) {

		Set<ICourse> unassigned = new HashSet<ICourse>();

		while (!courses.isEmpty()) {

			ICourse critical = session.getMostCriticalEvent(courses);
			int periods = session.getPeriodCount(critical);

			if (periods >= critical.getNumberOfLectures()) {
				session.assignRandomViableSlots(critical);
			} else {
				unassigned.add(critical);
			}
			courses.remove(critical);
		}

		return unassigned;
	}
}

class SessionObject {

	private final IProblemInstance instance;

	private final Map<ICourse, List<Integer>> availableSlots = new HashMap<ICourse, List<Integer>>();

	private final Map<ICourse, Integer> availablePeriodsCount = new HashMap<ICourse, Integer>();

	private final List<Set<ICurriculum>> curriculaInPeriod = new ArrayList<Set<ICurriculum>>();

	private final List<Set<String>> teachersInPeriod = new ArrayList<Set<String>>();

	private final ICourse[] schedule;

	private boolean calculateSlots = true;

	private final List<ICourse> priorityList = new ArrayList<ICourse>();

	public SessionObject(final IProblemInstance instance) {
		this.instance = instance;
		int slots = instance.getNumberOfPeriods() * instance.getNumberOfRooms();
		schedule = new ICourse[slots];

		for (int i = 0; i < instance.getNumberOfPeriods(); i++) {
			curriculaInPeriod.add(new HashSet<ICurriculum>());
			teachersInPeriod.add(new HashSet<String>());
		}
	}

	public ICourse getMostCriticalEvent(final Set<ICourse> courses) {
		if (calculateSlots) {
			calculateSlots(courses);
		}

		ICourse critical = priorityList.get(0);
		priorityList.remove(0);

		if (priorityList.isEmpty()) {
			calculateSlots = true;
		}

		return critical;
	}

	public void assignRandomViableSlots(final ICourse course) {
		for (int i = 0; i < course.getNumberOfLectures(); i++) {

			List<Integer> slots = availableSlots.get(course);
			java.util.Collections.shuffle(slots);
			int randomSlot = slots.get(0);
			schedule[randomSlot] = course;

			int period = getPeriodForSlot(randomSlot);
			curriculaInPeriod.get(period).addAll(course.getCurricula());
			teachersInPeriod.get(period).add(course.getTeacher());

			List<Integer> remainingSlots = new ArrayList<Integer>();

			for (int slot : slots) {
				if (period != getPeriodForSlot(slot)) {
					remainingSlots.add(slot);
				}
			}
			availableSlots.put(course, remainingSlots);
		}

		calculateSlots = true;
	}

	public ICourse[][] getCoding() {
		ICourse[][] coding = new ICourse[instance.getNumberOfPeriods()][instance
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

	public int getPeriodCount(final ICourse course) {
		return availablePeriodsCount.get(course);
	}

	private void calculateSlots(final Set<ICourse> courses) {
		reset(courses);

		for (ICourse course : courses) {

			int i = 0;

			while (i < schedule.length) {
				int period = getPeriodForSlot(i);

				if (slotInvalid(course, period)) {
					i += instance.getNumberOfRooms();
				} else {
					int periodEnd = i + instance.getNumberOfRooms();

					while (i < periodEnd) {
						if (schedule[i] == null) {
							availableSlots.get(course).add(i);
						}

						i++;
					}

					int periodCount = availablePeriodsCount.get(course);
					periodCount++;
					availablePeriodsCount.put(course, periodCount);
				}
			}
		}

		addMissingCourses(courses);
		generatePriorityList();

		calculateSlots = false;
	}

	private int getPeriodForSlot(final int slot) {
		return slot / instance.getNumberOfRooms();
	}

	private boolean slotInvalid(ICourse course, int period) {
		Set<ICurriculum> intersection = new HashSet<ICurriculum>();
		intersection.addAll(curriculaInPeriod.get(period));
		intersection.retainAll(course.getCurricula());

		return (intersection.size() > 0)
				|| teachersInPeriod.get(period).contains(course.getTeacher())
				|| violatesUnavailabilityConstraints(course, period)
				|| allRoomsOccupied(period);
	}

	private boolean violatesUnavailabilityConstraints(final ICourse course,
			final int period) {
		return instance.getUnavailabilityConstraints(course).contains(period);
	}

	private boolean allRoomsOccupied(final int period) {
		int periodStart = period * instance.getNumberOfRooms();
		int periodEnd = periodStart + instance.getNumberOfRooms();

		for (int i = periodStart; i < periodEnd; i++) {
			if (schedule[i] == null) {
				return false;
			}
		}

		return true;
	}

	private void addMissingCourses(final Set<ICourse> courses) {
		for (ICourse course : courses) {
			if (!availablePeriodsCount.keySet().contains(course)) {
				availablePeriodsCount.put(course, 0);
			}
		}
	}

	private void generatePriorityList() {

		boolean changed = true;

		while (changed) {
			changed = false;

			for (int i = 0; i < priorityList.size() - 1; i++) {

				int slotCount_1 = availableSlots.get(priorityList.get(i))
						.size();

				int slotCount_2 = availableSlots.get(priorityList.get(i + 1))
						.size();

				if (slotCount_1 > slotCount_2) {
					ICourse course_1 = priorityList.get(i);
					ICourse course_2 = priorityList.get(i + 1);
					priorityList.set(i, course_2);
					priorityList.set(i + 1, course_1);

					changed = true;
				}
			}
		}
	}

	private void reset(final Set<ICourse> courses) {
		priorityList.clear();
		priorityList.addAll(courses);
		availableSlots.clear();
		availablePeriodsCount.clear();

		for (ICourse course : courses) {
			availableSlots.put(course, new ArrayList<Integer>());
			availablePeriodsCount.put(course, 0);
		}
	}
}