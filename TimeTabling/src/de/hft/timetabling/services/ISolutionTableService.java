package de.hft.timetabling.services;

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
	 */
	ISolution createNewSolution(String[][] coding);

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
	void setSolution(int solutionNumber, ISolution solution);

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
	 * @param vote
	 *            The value of the vote.
	 * 
	 * @throws RuntimeException
	 *             If the given solution is not currently stored in the solution
	 *             table or if the given solution was already voted for.
	 */
	void voteForSolution(ISolution solution, int vote);

	/**
	 * Returns the vote sum for the given solution.
	 * 
	 * @param solution
	 *            The solution to retrieve the vote sum for.
	 * 
	 * @throws RuntimeException
	 *             If the given solution is not currently stored in the solution
	 *             table.
	 */
	int getVoteSumForSolution(ISolution solution);

	/**
	 * Returns the best solution so far or <tt>null</tt> if no best solution is
	 * available yet.
	 */
	ISolution getBestSolution();

}
