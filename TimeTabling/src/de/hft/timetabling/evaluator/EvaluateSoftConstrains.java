package de.hft.timetabling.evaluator;

import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.IRoom;
import de.hft.timetabling.common.ISolution;

public class EvaluateSoftConstrains {

	/**
	 * Evaluate the soft constrains for each curriculum and store the penalty
	 * points
	 * 
	 * @author Roy
	 */

	public EvaluateSoftConstrains() {
		// constructor
	}

	private IProblemInstance currentInstance;
	private ICourse[][] currentCode;
	private IRoom currentRoom;
	private ICourse currentCourseDetails;

	/**
	 * This method calculates the penalty in Room Capacity
	 * 
	 * @param solution
	 *            A solution instance is send for evaluation
	 * @return iCost Returns the penalty value
	 */

	// needs to be based on curriculum to have separate penalty points for each
	public int CostsOnRoomCapacity(ISolution solution, ICurriculum curriculum) {
		int iCost = 0;
		int iNoOfStudents, iRoomCapacity;
		int r, p;
		Set<ICourse> courses;
		String strCourse;

		// Check if the ISolution already has a penalty value
		// ** new/recombined solutions should not have old penalty values
		/*
		 * Code to be added
		 */

		// Get the problem instance to get the values related to it
		currentInstance = solution.getProblemInstance();
		// Get the solution array for this ISolution
		currentCode = solution.getCoding();

		// iNoOfStudents = currentCourse.getNumberOfStudents();
		// currentInstance.getCurricula();getNumberOfRooms();
		// currentRoom.getCapacity();

		// Get the courses for the curriculum
		courses = curriculum.getCourses();

		// currentInstance.getCourses();

		for (p = 0; p < currentInstance.getNumberOfPeriods(); p++) {
			for (r = 0; r < currentInstance.getNumberOfRooms(); r++) {
				strCourse = currentCode[p][r].getId();
				// Assuming the value is null if no course is assigned
				// the course should be contained in the curriculum
				if ((strCourse != null) && courses.contains(strCourse)) {
					iNoOfStudents = currentCode[p][r].getNumberOfStudents();
					currentRoom = currentInstance.getRoomByUniqueNumber(r);
					iRoomCapacity = currentRoom.getCapacity();
					// Each student above the capacity counts as 1 point of
					// penalty
					if (iNoOfStudents > iRoomCapacity) {
						iCost += (iNoOfStudents - iRoomCapacity);
					}
				}
			}
		}

		return iCost;
	}

	/**
	 * Method to calculate the penalty on min working days
	 * 
	 * @param solution
	 *            A solution instance is send for evaluation
	 * @return iCost Returns the penalty value
	 */
	// Need one more parameter to make it curriculum specific
	public int CostsOnMinWorkingDays(ISolution solution, ICurriculum curriculum) {
		int iCost = 0;
		int p, r, iNumberOfCourses, iWorkingDays;
		int iMinWorkingDays, iPeriodPerDay;
		Set<ICourse> courses;
		String strCourse;
		String ArrayCourse[];
		ICourse Course;

		currentInstance = solution.getProblemInstance();
		currentCode = solution.getCoding();
		courses = curriculum.getCourses();

		iPeriodPerDay = currentInstance.getPeriodsPerDay();
		iNumberOfCourses = curriculum.getNumberOfCourses();
		ArrayCourse = new String[iNumberOfCourses];
		// curriculum.containsCourse(courses);
		/*
		 * do { int i = 0; ArrayCourse[i] =
		 * courses.iterator().next().toString(); i++; } while
		 * (courses.iterator().hasNext());
		 */

		for (int i = 0; i < iNumberOfCourses; i++) {
			Course = courses.iterator().next();
			ArrayCourse[i] = Course.toString();
			iMinWorkingDays = Course.getMinWorkingDays();
			iWorkingDays = 0;
			boolean bDay = false;
			for (p = 0; p < currentInstance.getNumberOfPeriods(); p++) {
				// the course should count only once per day
				if (p % iPeriodPerDay == 0) {
					bDay = true;
				}
				for (r = 0; r < currentInstance.getNumberOfRooms(); r++) {
					strCourse = currentCode[p][r].getId();
					// Assuming the value is null if no course is assigned
					// the course should be contained in the curriculum
					if ((strCourse != null) && (ArrayCourse[i] == strCourse)) {

						// iMinWorkingDays =
						// currentCode[p][r].getMinWorkingDays();

						// Check the flag for day
						if (bDay) {
							iWorkingDays++;
							bDay = false;
						}
					}
				}
			}
			// Each day below the minimum counts as 5 points of penalty
			if (iWorkingDays < iMinWorkingDays) {
				iCost += (iMinWorkingDays - iWorkingDays) * 5;
			}

		}
		return iCost;
	}
}
