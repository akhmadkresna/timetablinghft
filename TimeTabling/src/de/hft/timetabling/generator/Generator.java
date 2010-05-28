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

class Generator {

	private final IProblemInstance instance;

	private Map<ICourse, List<Integer>> availableSlots = new HashMap<ICourse, List<Integer>>();
	private Map<ICourse, Integer> availablePeriods = new HashMap<ICourse, Integer>();

	private final int periods;
	private final int slots;

	private List<Set<ICurriculum>> curriculaInPeriod;
	private List<Set<String>> teachersInPeriod;

	public Generator(IProblemInstance instance) {
		this.instance = instance;
		periods = instance.getNumberOfDays() * instance.getPeriodsPerDay();
		slots = periods * instance.getNumberOfRooms();

		for (int i = 0; i < periods; i++) {
			curriculaInPeriod.add(new HashSet<ICurriculum>());
			teachersInPeriod.add(new HashSet<String>());
		}
	}

	public void generateFeasibileSolutin() {
		Set<ICourse> prioterized = new HashSet<ICourse>();
		Set<ICourse> nonPrioterized = new HashSet<ICourse>();
		Set<ICourse> unassigned = new HashSet<ICourse>();

		int maxLoops = 10;
		int loops = 0;

		// ICourse[][] schedule = new
		// ICourse[periods][instance.getNumberOfRooms()];
		ICourse[] schedule = new ICourse[slots];

		while (!unassigned.isEmpty() || (loops < maxLoops)) {
			prioterized.addAll(unassigned);
			unassigned = new HashSet<ICourse>();
			nonPrioterized = new HashSet<ICourse>();
			nonPrioterized.addAll(instance.getCourses());
			nonPrioterized.removeAll(prioterized);

			while (!prioterized.isEmpty()) {
				ICourse critical = getMostCriticalEvent(schedule, prioterized);

				if (availablePeriods.get(critical) >= critical
						.getNumberOfLectures()) {
					assignRandomViableSlots(critical, schedule);
				} else {
					unassigned.add(critical);
				}
				prioterized.remove(critical);
			}

			while (!nonPrioterized.isEmpty()) {
				ICourse critical = getMostCriticalEvent(schedule,
						nonPrioterized);

				if (availablePeriods.get(critical) > critical
						.getNumberOfLectures()) {
					assignRandomViableSlots(critical, schedule);
				} else {
					unassigned.add(critical);
				}

				nonPrioterized.remove(critical);
			}

			loops++;
		}
	}

	private ICourse getMostCriticalEvent(ICourse[] schedule,
			Set<ICourse> courses) {

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
				 * Whole range corresponding to a single period will be skipped
				 * if a) There are already courses from the same curriculum (any
				 * curriculum) in this period or b) the teacher holding the
				 * course is already giving another lecture in this period or c)
				 * unavailability constraints are violated
				 */
				Set<ICurriculum> intersectionCurricula = new HashSet<ICurriculum>();
				intersectionCurricula.addAll(curriculaInPeriod.get(period));
				intersectionCurricula.retainAll(course.getCurricula());

				if ((intersectionCurricula.size() > 0)
						|| teachersInPeriod.get(period).contains(
								course.getTeacher())
						|| violatesConstraints(course, period)) {

					/*
					 * skip range belonging to current period
					 */
					i += instance.getNumberOfRooms();
					continue;
				}

				/*
				 * Add available slots
				 */
				int j = i;
				while (i < j + instance.getNumberOfRooms()) {
					if (schedule[i] == null) {
						availableSlots.get(course).add(i);
					}
					i++;
				}
				int p = availablePeriods.get(course);
				availablePeriods.put(course, p++);
			}
		}

		/*
		 * Determine the course with the least number of available timeslots
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
}
