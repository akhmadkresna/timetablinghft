package de.hft.timetabling.genetist;

import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.ServiceLocator;

/**
 * Abstract base class that any recombination strategy that is written for our
 * project has to extend from. Recombination is the process of creating a new
 * solution from a number of given parent solutions.
 * <p>
 * The recombination strategy also incorporates the used mutation approach,
 * elimination strategy and parameter configurations.
 * 
 * @author Alexander Weickmann
 */
public abstract class RecombinationStrategy {

	/**
	 * Recombines the given solutions in order create a new solution. Returns
	 * the newly created solution or <tt>null</tt> if recombination was not
	 * possible because the solutions are not compatible.
	 * 
	 * @param solution1
	 *            The first of the two parent solutions to recombine.
	 * @param solution2
	 *            The second of the two parent solutions to recombine.
	 */
	public abstract ISolution recombine(ISolution solution1, ISolution solution2);

	/**
	 * Responsible for resetting the recombination algorithm so that a new
	 * recombination can be performed.
	 */
	protected abstract void reset();

	protected final ISolutionTableService getSolutionTable() {
		return ServiceLocator.getInstance().getSolutionTableService();
	}

	/**
	 * Checks whether the given teacher is already holding a course in the given
	 * period (<tt>true</tt>) or not (<tt>false</tt>).
	 * 
	 * @param coding
	 *            The coding that should be analyzed.
	 * @param teacher
	 *            The teacher to search for.
	 * @param period
	 *            The period (period-only format) to be inspected.
	 */
	protected final boolean existsTeacherInPeriod(ICourse[][] coding,
			String teacher, int period) {

		for (int room = 0; room < coding[period].length; room++) {
			if (coding[period][room] == null) {
				continue;
			}
			if (coding[period][room].getTeacher().equals(teacher)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether one of the given curricula has already courses in the
	 * given period (<tt>true</tt>) or not (<tt>false</tt>).
	 * 
	 * @param coding
	 *            The coding that should be analyzed.
	 * @param curriculua
	 *            The curriculua to search for.
	 * @param period
	 *            The period (period-only format) to be inspected.
	 */
	protected final boolean existsCurriculaInPeriod(ICourse[][] coding,
			Set<ICurriculum> curricula, int period) {

		for (int room = 0; room < coding[period].length; room++) {
			if (coding[period][room] == null) {
				continue;
			}
			for (ICurriculum curriculum : coding[period][room].getCurricula()) {
				if (curricula.contains(curriculum)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks whether there exists an unavailability constraint for the given
	 * course in the given period (<tt>true</tt>) or not (<tt>false</tt>).
	 * 
	 * @param course
	 *            The course to look for unavailability constraints.
	 * @param period
	 *            The period (period-only format) to look for unavailability
	 *            constraints.
	 */
	protected final boolean existsUnavailabilityConstraint(ICourse course,
			int period) {

		IProblemInstance instance = course.getProblemInstance();
		return instance.getUnavailabilityConstraints(course).contains(period);
	}

	public abstract String getName();

	/**
	 * Called after successful recombination, responsible for mutating the
	 * recombined solution.
	 * 
	 * @param recombinedSolution
	 *            The solution created during recombination that has to be
	 *            mutated.
	 */
	protected abstract ISolution mutate(ISolution recombinedSolution);

	/**
	 * Called after successful recombination and mutation, right before the
	 * recombined solution will be added to the solution table. This operation
	 * is responsible for eliminating other solutions to make space for the new
	 * solution.
	 * <p>
	 * Note that at least one solution should be eliminated from the solution
	 * table to make sure that the solution table does not overflow when the
	 * created child solution is added.
	 * <p>
	 * All eliminated solutions must be added to the set provided.
	 * 
	 * @param parent1
	 *            The first parent of the created child solution.
	 * @param parent2
	 *            The second parent of the created child solution.
	 * @param eliminatedSolutions
	 *            All eliminated solutions must be added to this set.
	 */
	protected abstract void eliminate(ISolution parent1, ISolution parent2,
			Set<ISolution> eliminatedSolutions);

	/**
	 * Called before the whole recombination process is started. The meaning of
	 * this operation is to enable the recombination strategy to set up
	 * different parameters like for example solution table size.
	 */
	protected abstract void configure();

	/**
	 * Returns the percentage of how many solutions are recombined per
	 * iteration.
	 */
	public abstract int getRecombinationPercentage();

}
