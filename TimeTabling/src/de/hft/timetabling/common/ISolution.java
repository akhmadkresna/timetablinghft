package de.hft.timetabling.common;

/**
 * A solution is a result of the program. Feasible solutions are created by the
 * generator sub system. A solution can be used as a basis for further
 * solutions. To create new solutions based on existing ones, the genetist sub
 * system is responsible.
 * 
 * @author Alexander Weickmann
 */
public interface ISolution {

	/**
	 * Returns the coding for this solution. The x-dimension of the returned
	 * array represents periods, while the y-dimension represents rooms. The
	 * content of the array are course objects. So overall, a course is
	 * associated to a specific period and room.
	 */
	ICourse[][] getCoding();

	/**
	 * Returns the problem instance this is a solution for.
	 */
	IProblemInstance getProblemInstance();

	/**
	 * Method to get the numbers of recombinations.
	 * 
	 * @return number of recombinations
	 * @author Steffen
	 */
	int getRecombinationCount();

	/**
	 * Set the number of recombinations
	 * 
	 * @param nrOfRecombinations
	 * @author Steffen
	 */
	void setRecombinationCount(int nrOfRecombinations);

	/**
	 * Method to increase the number of recombinations.
	 * 
	 * @author Steffen
	 */
	void increaseRecombinationCount();

	/**
	 * Method to clone a solution
	 * 
	 * @return colne of a ISolution
	 * @author Steffen
	 */
	ISolution clone();

}
