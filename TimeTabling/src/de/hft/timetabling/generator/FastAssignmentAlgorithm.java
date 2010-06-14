package de.hft.timetabling.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IProblemInstance;

public class FastAssignmentAlgorithm implements GeneratorAlgorithm {

	private final IProblemInstance instance;

	private final HashMap<ICourse, Set<Integer>> availablePeriods = new HashMap<ICourse, Set<Integer>>();

	private final List<Set<Integer>> availableRooms = new ArrayList<Set<Integer>>();

	private final ICourse[][] schedule;

	public FastAssignmentAlgorithm(final IProblemInstance instance) {
		this.instance = instance;
		schedule = new ICourse[instance.getNumberOfPeriods()][instance
				.getNumberOfRooms()];

		for (final ICourse course : instance.getCourses()) {
			for (int period = 0; period < instance.getNumberOfPeriods(); period++) {
				if (availablePeriods.get(course) == null) {
					availablePeriods.put(course, new HashSet<Integer>());
				}

				if (!instance.getUnavailabilityConstraints(course).contains(
						period)) {
					availablePeriods.get(course).add(period);
				}
			}
		}

		for (int period = 0; period < instance.getNumberOfPeriods(); period++) {
			availableRooms.add(new HashSet<Integer>());

			for (int room = 0; room < instance.getNumberOfRooms(); room++) {
				availableRooms.get(period).add(room);
			}
		}
	}

	public ICourse getMostCriticalEvent(final Set<ICourse> courses) {
		int minimum = Integer.MAX_VALUE;
		final List<ICourse> criticalCourses = new ArrayList<ICourse>();

		for (final ICourse course : courses) {
			final int periods = availablePeriods.get(course).size();

			if (periods < minimum) {
				minimum = periods;
				criticalCourses.clear();
				criticalCourses.add(course);
			} else if (periods == minimum) {
				criticalCourses.add(course);
			}
		}
		Collections.shuffle(criticalCourses);

		return criticalCourses.get(0);
	}

	public ICourse[][] getCoding() {
		return schedule;
	}

	public boolean isAssignable(final ICourse course) {
		return availablePeriods.get(course).size() >= course
				.getNumberOfLectures();
	}

	public void assignRandomViableSlots(final ICourse critical) {
		final List<Integer> periods = new ArrayList<Integer>();
		periods.addAll(availablePeriods.get(critical));
		final Set<Integer> assignedPeriods = new HashSet<Integer>();

		for (int i = 0; i < critical.getNumberOfLectures(); i++) {
			Collections.shuffle(periods);
			final int randomPeriod = periods.get(0);

			final List<Integer> rooms = new ArrayList<Integer>();
			rooms.addAll(availableRooms.get(randomPeriod));

			if (rooms.size() == 1) {
				for (final ICourse course : instance.getCourses()) {
					availablePeriods.get(course).remove(randomPeriod);
				}
			}
			Collections.shuffle(rooms);
			final int randomRoom = rooms.get(0);
			schedule[randomPeriod][randomRoom] = critical;

			availableRooms.get(randomPeriod).remove(randomRoom);
			periods.remove(0);

			assignedPeriods.add(randomPeriod);
		}

		for (final ICurriculum curriculum : critical.getCurricula()) {
			for (final ICourse course : curriculum.getCourses()) {
				availablePeriods.get(course).removeAll(assignedPeriods);
			}
		}

		for (final ICourse course : instance.getCoursesForTeacher(critical
				.getTeacher())) {
			availablePeriods.get(course).removeAll(assignedPeriods);
		}
	}
}
