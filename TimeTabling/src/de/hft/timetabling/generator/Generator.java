package de.hft.timetabling.generator;

import java.util.HashSet;
import java.util.Set;

import de.hft.timetabling.common.ICourse;
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
public final class Generator implements IGeneratorService {
	/**
	 * the amount of iterations which will be performed before the algorithm
	 * gives up trying to create a feasible solution
	 */
	private final int MAX_ITERATIONS = 50;
	/**
	 * the amount of loops during each iteration. Higher loop counts increase
	 * the likelihood of a feasible solution to be found
	 */
	private final int MAX_LOOPS = 10;

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
			final GeneratorAlgorithm session = new FastAssignmentAlgorithm(
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
	private Set<ICourse> assignCourses(final GeneratorAlgorithm session,
			final Set<ICourse> courses) {
		final Set<ICourse> unassigned = new HashSet<ICourse>();

		while (!courses.isEmpty()) {
			final ICourse critical = session.getMostCriticalEvent(courses);

			if (session.isAssignable(critical)) {
				session.assignRandomViableSlots(critical);
			} else {
				unassigned.add(critical);
			}
			courses.remove(critical);
		}
		return unassigned;
	}

	@Override
	public void fillSolutionTable(final IProblemInstance problemInstance) {
		final ISolutionTableService solutionTable = ServiceLocator
				.getInstance().getSolutionTableService();
		final int numberOfEmptySlots = solutionTable.getNumberOfEmptySlots();
		System.out.print("GENERATOR: Filling " + numberOfEmptySlots
				+ " empty slots ...");

		while (solutionTable.getNumberOfEmptySlots() > 0) {
			ICourse[][] coding;
			try {
				coding = generateFeasibleSolution(problemInstance);
				final ISolution newSolution = solutionTable.createNewSolution(
						coding, problemInstance);
				solutionTable.addSolution(newSolution);
			} catch (final NoFeasibleSolutionFoundException e) {
				e.printStackTrace();
			}
		}
		System.out.print(" done.\n");
	}
}
