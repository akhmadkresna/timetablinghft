package de.hft.timetabling.services;

import java.util.List;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;

/**
 * The solution table service holds the central solution table. The number of
 * solutions that are held in the table simultaneously can be set by the method
 * {@link #setMaximumSize(int)}.
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

	/**
	 * Returns what is the maximum of how many solutions are held in the
	 * solution table simultaneously.
	 */
	int getMaximumSize();

	/**
	 * Sets the maximum of how many solutions are held in the solution table at
	 * the same time.
	 * 
	 * @param maximumSize
	 *            The maximum size of the solution table.
	 */
	void setMaximumSize(int maximumSize);

	/**
	 * Returns how many solution slots are currently empty.
	 */
	int getNumberOfEmptySlots();

	/**
	 * Returns whether the solution table is currently full which is the case if
	 * the value returned by <tt>getSize(true)</tt> equals the maximum table
	 * size.
	 */
	boolean isFull();

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
	 * Stores the given solution into the solution table.
	 * 
	 * @param solution
	 *            The solution to store in the solution table.
	 * 
	 * @throws RuntimeException
	 *             If the solution table is full.
	 */
	void addSolution(ISolution solution);

	/**
	 * Returns the best solution so far or <tt>null</tt> if there is none
	 * available yet. The best solution will always be the solution with the
	 * fewest penalty points.
	 */
	ISolution getBestPenaltySolution();

	/**
	 * Returns the fairest solution so far or <tt>null</tt> if there is none
	 * available yet. The fairest solution will always be the solution with the
	 * fewest fairness points.
	 */
	ISolution getBestFairnessSolution();

	ISolution getWorstPenaltySolution();

	ISolution getWorstFairnessSolution();

	void voteForSolution(int index, int penalty, int fairness);

	/**
	 * Returns a list containing all solutions that have not been voted yet.
	 */
	List<ISolution> getNotVotedSolutions();

	/**
	 * This method must be called once per main loop iteration. It updates the
	 * current best penalty solution as well as the current best fairness
	 * solution. Additional update actions that are absolutely mandatory are
	 * performed as well.
	 */
	void update();

	/**
	 * Returns how many solutions are currently stored in the solution table.
	 * 
	 * @param includeNotVotedSolutions
	 *            Flag indicating whether solutions that are not yet voted
	 *            should be counted as well.
	 */
	int getSize(boolean includeNotVotedSolutions);

	/**
	 * Removes the current worst solution from the solution table. Does nothing
	 * if the solution table is currently empty. Returns the solution that has
	 * been removed or <tt>null</tt> if the solution table was empty.
	 * <p>
	 * The solution will only be eliminated if it is at least as old as the
	 * given minimum age. If it is younger, the next worst solution will be
	 * eliminated and so on. If no solution is old enough however, the worst
	 * will be removed so this method eliminates at least 1 solution whatsoever
	 * (except when the solution table is empty).
	 * 
	 * @param minAge
	 *            The age that the solution must have at least in order to be
	 *            eliminated.
	 */
	ISolution removeWorstSolution(int minAge);

	/**
	 * Returns the solution that has been recombined most often. Returns
	 * <tt>null</tt> if the solution table is currently empty.
	 */
	ISolution getSolutionMostOftenRecombined();

	/**
	 * Removes the given solution from the solution table. Does nothing if the
	 * given solution is not contained in the solution table at this time.
	 * Returns whether the operation did indeed remove a solution from the table
	 * or not.
	 * 
	 * @param solution
	 *            The solution to remove from the solution table.
	 */
	boolean remove(ISolution solution);

	/**
	 * Returns the solution at the given index.
	 */
	ISolution getSolution(int index);

	/**
	 * compare parameter value with penalty of worst solution (last())
	 * 
	 * @author Roy
	 * 
	 * @param iPenalty
	 *            Penalty value
	 * 
	 * @return true if the Penalty given is better than worst solution
	 */
	boolean compareWithWorstSolution(int iPenalty);

	/**
	 * Clears all current entries in the table and prepares it for a complete
	 * new execution of the program in general.
	 */
	void clear();

}
