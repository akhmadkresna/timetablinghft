package de.hft.timetabling.services;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;

/**
 * The solution table service holds the central solution table. The number of
 * solutions that are held in the table simultaneously is defined by the
 * constant {@link #TABLE_SIZE}.
 * <p>
 * The service interface provides ways to set the solutions in the table and
 * retrieve information about them. It also enables clients to vote for the
 * individual solutions and to create completely new solution instances.
 * <p>
 * At all times, the best solution so far is stored separately. Trough the
 * interface it is possible to retrieve this best solution as well.
 * 
 * @author Alexander Weickmann
 */
public interface ISolutionTableService {

	/** Defines how many solution are held in the solution table. */
	int TABLE_SIZE = 10;

	/**
	 * Factory method allowing to create new solution instances.
	 * 
	 * @param coding
	 *            The coding of the solution to create. The x-dimension of the
	 *            array represents periods, while the y-dimension of the array
	 *            represents rooms. Each room has a unique number which can be
	 *            used as array index.
	 * @param problemInstance
	 *            The {@link IProblemInstance} the new solution is for.
	 * 
	 * @throws IllegalArgumentException
	 *             If the given coding is incomplete. A coding is considered
	 *             complete, if for every period x, there is an entry for each
	 *             room y and additionally, every period is contained in the
	 *             array. That means, if for example there are 10 periods and 2
	 *             rooms in total, the array's dimension must be 10-2.
	 */
	ISolution createNewSolution(ICourse[][] coding,
			IProblemInstance problemInstance);

	/**
	 * Replaces the solution in the table identified by the given solution
	 * number with the provided solution.
	 * 
	 * @param solutionNumber
	 *            The solution's number in the table.
	 * @param solution
	 *            The solution to store in the solution table at the given
	 *            solution number.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             If the given solution number is not inside the allowed range.
	 */
	void putSolution(int solutionNumber, ISolution solution);

	/**
	 * Returns the solution that's currently located at the given solution table
	 * number or <tt>null</tt> if there is not yet any solution stored at the
	 * given solution number.
	 * 
	 * @param solutionNumber
	 *            The number of the solution to retrieve from the solution
	 *            table.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             If the given solution number is not inside the allowed range.
	 */
	ISolution getSolution(int solutionNumber);

	/**
	 * Allows to vote for the given solution.
	 * 
	 * @param solution
	 *            The solution to vote for.
	 * @param penaltyPoints
	 *            The number of penalty points to give.
	 * 
	 * @throws RuntimeException
	 *             If the given solution is not currently stored in the solution
	 *             table.
	 */
	void voteForSolution(ISolution solution, int penaltyPoints);

	/**
	 * Allows to vote for the solution located at the specified solution number.
	 * 
	 * @param solutionNumber
	 *            The solution number identifying the solution to vote for.
	 * @param penaltyPoints
	 *            The number of penalty points to give.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             If the given solution number is not inside the allowed range.
	 */
	void voteForSolution(int solutionNumber, int penaltyPoints);

	/**
	 * Returns the sum of penalty points for the given solution.
	 * 
	 * @param solution
	 *            The solution to retrieve the penalty points for.
	 * 
	 * @throws RuntimeException
	 *             If the given solution is not currently stored in the solution
	 *             table.
	 */
	int getPenaltySumForSolution(ISolution solution);

	/**
	 * Returns the sum of penalty points for the solution identified by the
	 * given solution number.
	 * 
	 * @param solutionNumber
	 *            The solution number identifying the solution to obtain the
	 *            penalty points for.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             If the given solution number is not inside the allowed range.
	 */
	int getPenaltySumForSolution(int solutionNumber);

	/**
	 * Returns the best solution so far or <tt>null</tt> if no best solution is
	 * available yet. The best solution will always be the solution with the
	 * fewest penalty points.
	 */
	ISolution getBestSolution();

	/**
	 * Returns the penalty points of the best solution.
	 * 
	 * @throws RuntimeException
	 *             If there is no best solution available yet.
	 */
	int getBestSolutionPenaltySum();

	/**
	 * Method to get back the actual number of solutions.
	 * 
	 * @author Steffen
	 * @return number of solutions
	 */
	int getActualSolutionTableCount();
}
