package de.hft.timetabling.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IGenerator;
import de.hft.timetabling.common.IProblemInstance;

public class Generator2 implements IGenerator {

	private final int MAX_ITERATIONS = 20;

	private final int MAX_LOOPS = 10;

	public ICourse[][] generateFeasibleSolution(IProblemInstance instance)
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
				unassigned = new HashSet<ICourse>();
				nonPrioterized = new HashSet<ICourse>();
				nonPrioterized.addAll(instance.getCourses());
				nonPrioterized.removeAll(prioterized);

				unassigned.addAll(assignCourses(session, prioterized));
				unassigned.addAll(assignCourses(session, nonPrioterized));

			} while (!unassigned.isEmpty() && (loops++ < MAX_LOOPS));

			if (unassigned.isEmpty()) {
				System.out.println("SOLUTION FOUND");
				return session.getCoding();
			}

			iterations++;
		}

		throw new NoFeasibleSolutionFoundException();
	}

	private Set<ICourse> assignCourses(SessionObject session,
			Set<ICourse> courses) {

		Set<ICourse> unassigned = new HashSet<ICourse>();

		while (!courses.isEmpty()) {

			ICourse critical = session.getCritical(courses);
			int periods = session.getPeriodNumber(critical);

			if (periods >= critical.getNumberOfLectures()) {
				session.assignSlots(critical);
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

	private final int slots;

	private final Map<ICourse, List<Integer>> availableSlots = new HashMap<ICourse, List<Integer>>();

	private final Map<ICourse, Integer> availablePeriods = new HashMap<ICourse, Integer>();

	private final List<Set<ICurriculum>> curriculaInPeriod = new ArrayList<Set<ICurriculum>>();

	private final List<Set<String>> teachersInPeriod = new ArrayList<Set<String>>();

	private final ICourse[] schedule;

	private boolean slotsAssigned = true;

	private List<ICourse> priorityList = new ArrayList<ICourse>();

	public SessionObject(IProblemInstance instance) {
		this.instance = instance;
		slots = instance.getNumberOfPeriods() * instance.getNumberOfRooms();
		schedule = new ICourse[slots];

		for (int i = 0; i < instance.getNumberOfPeriods(); i++) {
			curriculaInPeriod.add(new HashSet<ICurriculum>());
			teachersInPeriod.add(new HashSet<String>());
		}
	}

	public ICourse getCritical(Set<ICourse> courses) {

		if (slotsAssigned) {
			reset(courses);

			for (ICourse course : courses) {

				int i = 0;

				while (i < schedule.length) {
					int period = getSlotPeriod(i);

					if (slotInvalid(course, period)) {
						i += instance.getNumberOfRooms();
					} else {
						int endPeriod = i + instance.getNumberOfRooms();

						while (i < endPeriod) {
							if (schedule[i] == null) {
								availableSlots.get(course).add(i);
							}

							i++;
						}

						int periodCount = availablePeriods.get(course);
						periodCount++;
						availablePeriods.put(course, periodCount);
					}
				}
			}

			fillMissingCourses(courses);
			generatePriorityList();

			// slotsAssigned = false;
		}

		ICourse critical = priorityList.get(0);
		priorityList.remove(0);
		return critical;
	}

	public void assignSlots(ICourse course) {
		for (int i = 0; i < course.getNumberOfLectures(); i++) {

			List<Integer> slots = availableSlots.get(course);
			java.util.Collections.shuffle(slots);
			int randomSlot = slots.get(0);
			schedule[randomSlot] = course;

			int period = getSlotPeriod(randomSlot);
			curriculaInPeriod.get(period).addAll(course.getCurricula());
			teachersInPeriod.get(period).add(course.getTeacher());

			List<Integer> remainingSlots = new ArrayList<Integer>();

			for (int t : slots) {
				if (period != getSlotPeriod(t)) {
					remainingSlots.add(t);
				}
			}
			availableSlots.put(course, remainingSlots);
		}

		slotsAssigned = true;
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

	public int getPeriodNumber(ICourse course) {
		return availablePeriods.get(course);
	}

	private int getSlotPeriod(int slot) {
		return slot / instance.getNumberOfRooms();
	}

	private boolean slotInvalid(ICourse course, int period) {
		Set<ICurriculum> intersection = new HashSet<ICurriculum>();
		intersection.addAll(curriculaInPeriod.get(period));
		intersection.retainAll(course.getCurricula());

		return (intersection.size() > 0)
				|| teachersInPeriod.get(period).contains(course.getTeacher())
				|| violatesConstraints(course, period)
				|| allRoomsOccupied(period);
	}

	private boolean violatesConstraints(ICourse course, int period) {
		return instance.getUnavailabilityConstraints(course).contains(period);
	}

	private boolean allRoomsOccupied(int period) {
		int slot = period * instance.getNumberOfRooms();
		int periodEnd = slot + instance.getNumberOfRooms();

		for (int i = slot; i < periodEnd; i++) {
			if (schedule[i] == null) {
				return false;
			}
		}

		return true;
	}

	private void fillMissingCourses(Set<ICourse> courses) {
		for (ICourse course : courses) {
			if (!availablePeriods.keySet().contains(course)) {
				availablePeriods.put(course, 0);
				availableSlots.put(course, new ArrayList<Integer>());
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

	private void reset(Set<ICourse> courses) {
		priorityList.clear();
		priorityList.addAll(courses);
		availableSlots.clear();
		availablePeriods.clear();

		for (ICourse course : courses) {
			availableSlots.put(course, new ArrayList<Integer>());
			availablePeriods.put(course, 0);
		}
	}
}