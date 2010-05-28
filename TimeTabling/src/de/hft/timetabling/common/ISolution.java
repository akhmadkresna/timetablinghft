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
	 * content of the array are course IDs. So overall, a course ID is
	 * associated to a specific period and room.
	 */
	String[][] getCoding();

	/**
	 * Returns the problem instance this is a solution for.
	 */
	IProblemInstance getProblemInstance();

}
