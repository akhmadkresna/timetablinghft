package de.hft.timetabling.evaluator;

import java.util.Iterator;
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

	// Check if the ISolution already has a penalty value in the ISolutionTable
	// and then do validation
	// ** new/recombined solutions should not have old penalty values (-1)
	/*
	 * Code to be added
	 */

	// After checking constrains send the message with problems to output
	/*
	 * Code to be added
	 */

	private IProblemInstance currentInstance;
	private ICourse[][] currentCode;
	private IRoom currentRoom;

	// private ICourse currentCourseDetails;

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
				// Assuming the value is null if no course is assigned
				// the course should be contained in the curriculum
				if ((currentCode[p][r] != null)
						&& courses.contains(currentCode[p][r])) {
					iNoOfStudents = currentCode[p][r].getNumberOfStudents();
					currentRoom = currentInstance.getRoomByUniqueNumber(r);
					iRoomCapacity = currentRoom.getCapacity();
					// Each student above the capacity counts as 1 point of
					// penalty
					if (iNoOfStudents > iRoomCapacity) {
						iCost += (iNoOfStudents - iRoomCapacity);
					}
					// There are no more courses in the same period but
					// different room
					break;
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
		int p, r, iWorkingDays;
		int iMinWorkingDays, iPeriodPerDay;
		Set<ICourse> courses;
		// String ArrayCourse[];
		ICourse Course;
		Iterator<ICourse> it;

		currentInstance = solution.getProblemInstance();
		currentCode = solution.getCoding();
		courses = curriculum.getCourses();

		iPeriodPerDay = currentInstance.getPeriodsPerDay();
		// iNumberOfCourses = curriculum.getNumberOfCourses();
		// System.out.println("The number of course:" + iNumberOfCourses);
		// ArrayCourse = new String[iNumberOfCourses];
		// curriculum.containsCourse(courses);
		/*
		 * do { int i = 0; ArrayCourse[i] =
		 * courses.iterator().next().toString(); i++; } while
		 * (courses.iterator().hasNext());
		 */

		// To convert set to array
		// String[] array = courses.toArray(new String[courses.size()]);

		// Use iterator to parse through set
		it = courses.iterator();
		while (it.hasNext()) {
			Course = it.next();
			// ArrayCourse[i] = Course.toString();
			iMinWorkingDays = Course.getMinWorkingDays();
			iWorkingDays = 0;
			boolean bDay = true;
			for (p = 0; p < currentInstance.getNumberOfPeriods(); p++) {
				// the course should count only once per day
				if (p % iPeriodPerDay == 0) {
					bDay = true;
				}
				// improve performance: jump to the next day
				// continue with loop, subtract 1 as loop increments p value by
				// 1 resulting the flag to true at next loop
				else if (bDay == false) {
					p += (iPeriodPerDay - (p % iPeriodPerDay)) - 1;
					// bDay = true;
					continue;
				}

				for (r = 0; r < currentInstance.getNumberOfRooms(); r++) {
					// Assuming the value is null if no course is assigned
					// the course should be equal to the course
					if ((currentCode[p][r] != null)
							&& (currentCode[p][r] == Course)) {

						// iMinWorkingDays =
						// currentCode[p][r].getMinWorkingDays();

						// Check the flag for day
						if (bDay) {
							iWorkingDays++;
							bDay = false;
						}
						// There are no more courses in the same period but
						// different room
						break;
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

	/**
	 * Method to calculate the penalty on curriculum compactness and room
	 * stability
	 * 
	 * @param solution
	 *            A solution instance is send for evaluation
	 * @return iCost Returns the penalty value of both soft constrains
	 */
	// Need one more parameter to make it curriculum specific
	public int CostsOnCurriculumCompactnessAndRoomStability(ISolution solution,
			ICurriculum curriculum) {
		int iCost = 0;
		int p, r, d, iPreviousRoom;
		int iPreviousPeriod;
		Set<ICourse> courses;
		String strCourse;
		IRoom previousRoom;

		currentInstance = solution.getProblemInstance();
		currentCode = solution.getCoding();
		courses = curriculum.getCourses();

		// Initial value of day and period
		d = 1;
		p = 0;
		// Make the looping for each day
		while (d < currentInstance.getNumberOfDays()) {
			iPreviousPeriod = -1;
			iPreviousRoom = -1;
			for (; p < currentInstance.getPeriodsPerDay() * d; p++) {
				for (r = 0; r < currentInstance.getNumberOfRooms(); r++) {
					strCourse = currentCode[p][r].getId();
					// Assuming the value is null if no course is assigned
					// the course should be contained in the curriculum
					if ((strCourse != null)
							&& courses.contains(currentCode[p][r])) {
						if ((iPreviousPeriod == -1) && (iPreviousRoom == -1)) {
							iPreviousPeriod = p;
							iPreviousRoom = r;
						}
						// there should not be another course of same curriculum
						// in different room in the same period
						break;
					}
				}
				// Check if not new day
				// if (p % currentInstance.getPeriodsPerDay()!= 0) {
				if ((iPreviousPeriod == -1) && (iPreviousRoom == -1)) {
					// Check if the previous period course is of the same
					// curriculum
					// if (!courses.contains(currentCode[p - 1][iPreviousRoom]))
					// {
					if (iPreviousPeriod != (p - 1)) {
						iCost += 2;
					}
					// Penalty for Room
					currentRoom = currentInstance.getRoomByUniqueNumber(r);
					previousRoom = currentInstance
							.getRoomByUniqueNumber(iPreviousRoom);
					if (currentRoom.getId() != previousRoom.getId()) {
						iCost++;
					}
				}
			}
		}
		return iCost;
	}

}
