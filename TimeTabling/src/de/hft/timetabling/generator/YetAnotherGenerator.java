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
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.main.Main;
import de.hft.timetabling.services.IGeneratorService;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.ServiceLocator;

/**
 * This class generates feasible solutions for all problem instances from the
 * International Timetabling Competition. Due to the non-deterministic
 * construction process the class does not guarantee that each time a feasible
 * solution is found.
 * 
 * @author Matthias Ruszala
 */
public final class YetAnotherGenerator implements IGeneratorService {
	/**
	 * the amount of iterations which will be performed before the algorithm
	 * gives up trying to create a feasible solution
	 */
	private final int MAX_ITERATIONS = 30;
	/**
	 * the amount of loops during each iteration. Higher loop counts increase
	 * the likelihood of a feasible solution to be found
	 */
	private final int MAX_LOOPS = 20;

	/**
	 * This method tries to construct a feasible solution for the given problem
	 * instance.
	 * 
	 * @param instance
	 *            the problem instance for which a solution is to be found
	 * @return the valid schedule
	 * @throws NoFeasibleSolutionFoundException
	 *             when no solution can be found within the set amount of
	 *             iterations
	 */
	public ICourse[][] generateFeasibleSolution(final IProblemInstance instance)
			throws NoFeasibleSolutionFoundException {
		int iterations = 0;
		/*
		 * each iteration performs one run of the construction algorithm as
		 * described by Geiger
		 */
		while (iterations < MAX_ITERATIONS) {
			int loops = 0;
			YetAnotherSessionObject session = new YetAnotherSessionObject(
					instance);

			final Set<ICourse> prioterized = new HashSet<ICourse>();
			final Set<ICourse> nonPrioterized = new HashSet<ICourse>();
			final Set<ICourse> unassigned = new HashSet<ICourse>();

			do {
				prioterized.addAll(unassigned);
				unassigned.clear();
				nonPrioterized.clear();
				nonPrioterized.addAll(instance.getCourses());
				nonPrioterized.removeAll(prioterized);

				unassigned.addAll(assignCourses(session, prioterized));
				unassigned.addAll(assignCourses(session, nonPrioterized));
			} while (!unassigned.isEmpty() && (loops++ < MAX_LOOPS));
			/*
			 * feasible solution found if there are no unassigned courses left
			 */
			if (unassigned.isEmpty()) {
				Main.generatorSuccess++;
				return session.getCoding();
			}
			Main.generatorFailure++;
			iterations++;
		}
		throw new NoFeasibleSolutionFoundException();
	}

	/**
	 * This method tries to assign all passed courses to slots in the schedule
	 * while not violating any hard constraints.
	 * 
	 * @param session
	 *            the session object used for constructing a feasible solution
	 * @param courses
	 *            the set of courses to be assigned to the schedule
	 * @return the set of courses which could not be assigned
	 */
	private Set<ICourse> assignCourses(final YetAnotherSessionObject session,
			final Set<ICourse> courses) {
		Set<ICourse> unassigned = new HashSet<ICourse>();

		while (!courses.isEmpty()) {

			ICourse critical = session.getMostCriticalEvent(courses);

			if (session.assignable(critical)) {
				session.assignRandomViableSlots(critical);
			} else {
				unassigned.add(critical);
			}
			courses.remove(critical);
		}
		return unassigned;
	}

	@Override
	public void fillSolutionTable(IProblemInstance problemInstance) {
		ISolutionTableService solutionTable = ServiceLocator.getInstance()
				.getSolutionTableService();
		int numberOfEmptySlots = solutionTable.getNumberOfEmptySlots();
		System.out.print("GENERATOR: Filling " + numberOfEmptySlots
				+ " empty slots ...");

		while (solutionTable.getNumberOfEmptySlots() > 0) {
			ICourse[][] coding;
			try {
				coding = generateFeasibleSolution(problemInstance);
				ISolution newSolution = solutionTable.createNewSolution(coding,
						problemInstance);
				solutionTable.addSolution(newSolution);
			} catch (NoFeasibleSolutionFoundException e) {
				e.printStackTrace();
			}
		}
		System.out.print(" done.\n");
	}
}

/**
 * This internal class is used during construction of a feasible solution. It
 * stores all necessary information needed during the construction of *one*
 * feasible solution and offers methods required by the construction algorithm.
 * Objects of this class are discarded after each failed attempt to construct a
 * feasible solution.
 * 
 * @author Matthias Ruszala
 */
class YetAnotherSessionObject {
	private final IProblemInstance instance;

	private final HashMap<ICourse, Set<Integer>> availablePeriods = new HashMap<ICourse, Set<Integer>>();

	private final ICourse[][] schedule;

	private final List<Set<Integer>> availableRooms = new ArrayList<Set<Integer>>();

	public YetAnotherSessionObject(final IProblemInstance instance) {
		this.instance = instance;
		schedule = new ICourse[instance.getNumberOfPeriods()][instance
				.getNumberOfRooms()];

		for (ICourse course : instance.getCourses()) {
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
		ICourse critical = null;
		int minimum = Integer.MAX_VALUE;

		for (ICourse course : courses) {
			int periods = availablePeriods.get(course).size();

			if (periods < minimum) {
				minimum = periods;
				critical = course;
			}
		}
		return critical;
	}

	public ICourse[][] getCoding() {
		return schedule;
	}

	public boolean assignable(final ICourse course) {
		return availablePeriods.get(course).size() >= course
				.getNumberOfLectures();
	}

	public void assignRandomViableSlots(final ICourse critical) {
		final List<Integer> periods = new ArrayList<Integer>();
		periods.addAll(availablePeriods.get(critical));
		final Set<Integer> occupiedPeriods = new HashSet<Integer>();

		for (int i = 0; i < critical.getNumberOfLectures(); i++) {
			Collections.shuffle(periods);

			int randomPeriod = periods.get(0);

			final List<Integer> rooms = new ArrayList<Integer>();
			rooms.addAll(availableRooms.get(randomPeriod));

			if (rooms.size() == 1) {
				for (final ICourse course : instance.getCourses()) {
					availablePeriods.get(course).remove(randomPeriod);
				}
			}

			Collections.shuffle(rooms);
			int randomRoom = rooms.get(0);
			schedule[randomPeriod][randomRoom] = critical;

			availableRooms.get(randomPeriod).remove(randomRoom);

			periods.remove(0);
			occupiedPeriods.add(randomPeriod);
		}
		for (ICurriculum curriculum : critical.getCurricula()) {
			for (ICourse course : curriculum.getCourses()) {
				availablePeriods.get(course).removeAll(occupiedPeriods);
			}
		}

		for (ICourse course : instance.getCoursesForTeacher(critical
				.getTeacher())) {
			availablePeriods.get(course).removeAll(occupiedPeriods);
		}
	}
}