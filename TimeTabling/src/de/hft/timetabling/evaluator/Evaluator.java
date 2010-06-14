package de.hft.timetabling.evaluator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.IRoom;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.services.IEvaluatorService;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.ServiceLocator;

public class Evaluator implements IEvaluatorService {

	/**
	 * Evaluate the soft constrains for each curriculum and store the penalty
	 * points
	 * 
	 * @author Roy
	 */

	public Evaluator() {
		// constructor
	}

	private IProblemInstance currentInstance;
	private ICourse[][] currentCode;
	private IRoom currentRoom;
	private ISolutionTableService solutionTable;

	/**
	 * This method calculates the penalty in Room Capacity. Needs to be based on
	 * curriculum to have separate penalty points for each
	 * 
	 * @param solution
	 *            A solution instance is send for evaluation
	 * @param curriculum
	 *            The curriculum to check penalty for
	 * @return iCost Returns the penalty value
	 */
	private int costsOnRoomCapacity(final ISolution solution,
			final ICurriculum curriculum) {
		int iCost = 0;
		int iNoOfStudents, iRoomCapacity;
		int r, p;
		String strArrayRoomCourse = "";
		Set<ICourse> courses;

		// Get the problem instance to get the values related to it
		currentInstance = solution.getProblemInstance();
		// Get the solution array for this ISolution
		currentCode = solution.getCoding();

		// Get the courses for the curriculum
		courses = curriculum.getCourses();

		for (p = 0; p < currentInstance.getNumberOfPeriods(); p++) {
			for (r = 0; r < currentInstance.getNumberOfRooms(); r++) {
				// Assuming the value is null if no course is assigned
				// the course should be contained in the curriculum
				if ((currentCode[p][r] != null)
						&& courses.contains(currentCode[p][r])) {
					currentRoom = currentInstance.getRoomByUniqueNumber(r);

					// string format to add to string array. This will be used
					// to compare with previous course and rooms
					final String str = ":" + currentCode[p][r].toString() + ","

					+ currentRoom.toString() + ";";
					if (!strArrayRoomCourse.contains(str)) {

						iNoOfStudents = currentCode[p][r].getNumberOfStudents();

						currentRoom = currentInstance.getRoomByUniqueNumber(r);
						iRoomCapacity = currentRoom.getCapacity();

						// Add string to array for next comparison
						strArrayRoomCourse = strArrayRoomCourse + str;

						// Each student above the capacity counts as 1 point of
						// penalty
						if (iNoOfStudents > iRoomCapacity) {
							iCost = iCost + iNoOfStudents - iRoomCapacity;
						}

						// There are no more courses in the same period but
						// different room
						break;
					}
				}
			}
		}

		return iCost;
	}

	/**
	 * Method to calculate the penalty on min working days Need one more
	 * parameter to make it curriculum specific
	 * 
	 * @param solution
	 *            A solution instance is send for evaluation
	 * @param curriculum
	 *            The curriculum to check penalty for
	 * 
	 * @return iCost Returns the penalty value
	 */
	private int costsOnMinWorkingDays(final ISolution solution,
			final ICurriculum curriculum) {
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

		// To convert set to array
		// String[] array = courses.toArray(new String[courses.size()]);

		// Use iterator to parse through set
		it = courses.iterator();
		while (it.hasNext()) {
			Course = it.next();
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
				final int temp = iMinWorkingDays - iWorkingDays;
				final int temp2 = temp * 5;
				iCost += temp2;
			}

		}
		return iCost;
	}

	/**
	 * Method to calculate the penalty room stability. Need one more parameter
	 * to make it curriculum specific
	 * 
	 * @param solution
	 *            A solution instance is send for evaluation
	 * @param curriculum
	 *            The curriculum to check penalty for
	 * 
	 * @return iCost Returns the penalty value of both soft constrains
	 */
	private int costsOnRoomStability(final ISolution solution,
			final ICurriculum curriculum) {
		int iCost = 0;
		int p, r, rooms;
		Iterator<ICourse> it;
		Set<ICourse> courses;
		ICourse Course;

		currentInstance = solution.getProblemInstance();
		currentCode = solution.getCoding();
		courses = curriculum.getCourses();
		rooms = currentInstance.getNumberOfRooms();
		final int roomArray[] = new int[rooms];

		// Initial value of day and period
		it = courses.iterator();
		while (it.hasNext()) {
			int c = 0;
			for (int i = 0; i < rooms; i++) {
				roomArray[i] = -1;
			}
			Course = it.next();

			for (p = 0; p < currentInstance.getNumberOfPeriods(); p++) {
				for (r = 0; r < currentInstance.getNumberOfRooms(); r++) {
					// Assuming the value is null if no course is assigned
					// the course should be contained in the curriculum
					if ((currentCode[p][r] != null)
							&& (currentCode[p][r] == Course)) {
						if (c == 0) {
							roomArray[c] = r;
							c++;
						} else {
							boolean bRoomFromList = false;
							// if room is found in array don't do not add
							// penality again
							for (int i = 0; i < c; i++) {
								if (roomArray[i] == r) {
									bRoomFromList = true;
									break;
								}
							}
							// add rooms to array that are not already in list
							if (!bRoomFromList) {
								roomArray[c] = r;
								c++;
							}
						}

						// there should not be another course of same
						// curriculum in different room in the same period
						break;
					}
				}

			}
			iCost += (c - 1);
		}
		return iCost;
	}

	/**
	 * This method is called from the interface to start evaluation
	 * 
	 */
	@Override
	public void evaluateSolutions() {
		final ServiceLocator serviceLocator = ServiceLocator.getInstance();
		solutionTable = serviceLocator.getSolutionTableService();
		callSoftConstrainEvalutors(solutionTable);
	}

	/**
	 * Call the individual soft constrain evaluators for each solution
	 * 
	 * @param solutionTable
	 *            The solutionTable from which solution are taken
	 * 
	 */
	private void callSoftConstrainEvalutors(
			final ISolutionTableService solutionTable) {
		Set<ICurriculum> currentCurriculumSet;
		ICurriculum currentCurricula;
		int iFairness;
		final List<Integer> curriculumCosts = new ArrayList<Integer>();

		int iPenalty = 0;
		int iCurriculumBasedPenalty = 0;
		int iCurriculumCompactness = 0;
		int iCBasedRoom = 0;
		int iMinWorking = 0;
		int iCBasedRoomStability = 0;
		int iRoomCapacity = 0;
		int iCBasedMinWorking = 0;
		int iCBasedCompactness = 0;
		int iRoomStability = 0;

		final List<ISolution> notVotedSolutions = solutionTable
				.getNotVotedSolutions();
		final int nrNotVoted = notVotedSolutions.size();
		for (int i = 0; i < nrNotVoted; i++) {
			final ISolution solutionCode = notVotedSolutions.get(i);
			currentInstance = solutionCode.getProblemInstance();
			currentCode = solutionCode.getCoding();
			currentCurriculumSet = currentInstance.getCurricula();
			final Iterator<ICurriculum> it = currentCurriculumSet.iterator();
			iPenalty = 0;
			iCBasedRoom = 0;
			iCBasedMinWorking = 0;
			iCBasedCompactness = 0;
			iCBasedRoomStability = 0;

			// Iterate through each curriculum
			while (it.hasNext()) {
				currentCurricula = it.next();
				// Penalty calculation for given solution
				iCurriculumBasedPenalty += costsOnRoomCapacity(solutionCode,
						currentCurricula);
				iCurriculumBasedPenalty += costsOnMinWorkingDays(solutionCode,
						currentCurricula);
				iCurriculumCompactness += costsOnCurriculumCompactness(
						solutionCode, currentCurricula);

				iCurriculumBasedPenalty += iCurriculumCompactness;
				iCurriculumBasedPenalty += costsOnRoomStability(solutionCode,
						currentCurricula);
				curriculumCosts.add(iCurriculumBasedPenalty);

				// Debug code
				iCBasedRoom += costsOnRoomCapacity(solutionCode,
						currentCurricula);
				iCBasedMinWorking += costsOnMinWorkingDays(solutionCode,
						currentCurricula);
				iCBasedRoomStability += costsOnRoomStability(solutionCode,
						currentCurricula);
				iCBasedCompactness += costsOnCurriculumCompactness(
						solutionCode, currentCurricula);
			}
			iRoomCapacity += costsOnRoomCapacity(solutionCode);
			iMinWorking += costsOnMinWorkingDays(solutionCode);
			// iCurriculumCompactness
			iRoomStability += costsOnRoomStability(solutionCode);

			iPenalty = iRoomCapacity + iMinWorking + iCurriculumCompactness
					+ iRoomStability;
			iFairness = evaluateFairness(curriculumCosts);
			solutionTable.voteForSolution(i, iPenalty, iFairness);

			System.out.println("RoomCapacity: " + iCBasedRoom);
			System.out.println("MinWorking day: " + iCBasedMinWorking);
			System.out.println("CirriculumComp: " + iCBasedCompactness);
			System.out.println("RoomStablity: " + iCBasedRoomStability);
			System.out.println("NewRoomC: " + iRoomCapacity);
			System.out.println("NewMinWCost: " + iMinWorking);
			System.out.println("NewcompactCost: " + iCurriculumCompactness);
			System.out.println("NewRoomStability: " + iRoomStability);

		}
	}

	/**
	 * Calculates the fairness based on the max, min and avg value of Penalty
	 * for the curriculum. The lower the difference, the better the solution.
	 * 
	 * @param curriculumCosts
	 *            The integer list with the penalty for each curriculum
	 */
	public int evaluateFairness(final List<Integer> curriculumCosts) {
		int iFairnessCost = 0, maxAvgDiff, minAvgDiff;
		int maxPenalty = -1, minPenalty = -1, avgPenalty = -1, penaltySum = 0;

		// initial value to compare with
		maxPenalty = curriculumCosts.get(0);
		minPenalty = curriculumCosts.get(0);
		for (int i = 1; i < curriculumCosts.size(); i++) {
			// Take max value, min value, and average value... and compare
			if (curriculumCosts.get(i) > maxPenalty) {
				maxPenalty = curriculumCosts.get(i);
			}
			if (curriculumCosts.get(i) < minPenalty) {
				minPenalty = curriculumCosts.get(i);
			}
			penaltySum += curriculumCosts.get(i);
		}
		avgPenalty = (penaltySum / curriculumCosts.size());

		maxAvgDiff = maxPenalty - avgPenalty;
		minAvgDiff = avgPenalty - minPenalty;

		if (maxAvgDiff >= minAvgDiff) {
			// iFairnessCost = maxAvgDiff;
			iFairnessCost = maxAvgDiff;
		} else {
			// iFairnessCost = minAvgDiff;
			iFairnessCost = minAvgDiff;
		}

		return iFairnessCost;
	}

	@Override
	public int evaluateSolution(final ISolution newSolution) {
		Set<ICurriculum> currentCurriculumSet;
		ICurriculum currentCurricula;
		currentInstance = newSolution.getProblemInstance();
		currentCode = newSolution.getCoding();
		currentCurriculumSet = currentInstance.getCurricula();
		final Iterator<ICurriculum> it = currentCurriculumSet.iterator();
		int iPenalty = 0;

		while (it.hasNext()) {
			currentCurricula = it.next();
			// Penalty calculation of compactness based on curriculum for given
			// solution
			iPenalty += costsOnCurriculumCompactness(newSolution,
					currentCurricula);
		}
		// Penalty calculation for rest of the soft constrains
		iPenalty += costsOnRoomCapacity(newSolution);
		iPenalty += costsOnMinWorkingDays(newSolution);
		iPenalty += costsOnRoomStability(newSolution);

		return iPenalty;
	}

	/**
	 * This method calculates the penalty in Room Capacity. This is not based on
	 * curriculum but for entire solution
	 * 
	 * @param solution
	 *            A solution instance is send for evaluation
	 * @return iCost Returns the penalty value
	 */

	private int costsOnRoomCapacity(final ISolution solution) {
		int iCost = 0;
		int iNoOfStudents, iRoomCapacity;
		int r, p;

		// Get the problem instance to get the values related to it
		currentInstance = solution.getProblemInstance();
		// Get the solution array for this ISolution
		currentCode = solution.getCoding();

		for (p = 0; p < currentInstance.getNumberOfPeriods(); p++) {
			for (r = 0; r < currentInstance.getNumberOfRooms(); r++) {
				// Assuming the value is null if no course is assigned
				// the course should be contained in the curriculum
				if ((currentCode[p][r] != null)) {

					iNoOfStudents = currentCode[p][r].getNumberOfStudents();

					currentRoom = currentInstance.getRoomByUniqueNumber(r);
					iRoomCapacity = currentRoom.getCapacity();

					// Each student above the capacity counts as 1 point of
					// penalty
					if (iNoOfStudents > iRoomCapacity) {
						iCost = iCost + iNoOfStudents - iRoomCapacity;
					}
				}
			}
		}

		return iCost;
	}

	/**
	 * Method to calculate the penalty on min working days. This method is based
	 * on whole solution and not the curriculum
	 * 
	 * @param solution
	 *            A solution instance is send for evaluation
	 * @return iCost Returns the penalty value
	 */
	private int costsOnMinWorkingDays(final ISolution solution) {

		int iCost = 0;
		int p, r, iWorkingDays;
		int iMinWorkingDays, iPeriodPerDay;
		Set<ICourse> courses;
		ICourse Course;
		Iterator<ICourse> it;

		currentInstance = solution.getProblemInstance();
		currentCode = solution.getCoding();
		courses = currentInstance.getCourses();

		iPeriodPerDay = currentInstance.getPeriodsPerDay();

		// To convert set to array
		// String[] array = courses.toArray(new String[courses.size()]);

		// Use iterator to parse through set
		it = courses.iterator();
		while (it.hasNext()) {
			Course = it.next();
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

						// Check the flag for day
						if (bDay) {
							iWorkingDays++;
							bDay = false;
						}
						// The same courses is not taken into consideration for
						// same period
						break;
					}
				}
			}
			// Each day below the minimum counts as 5 points of penalty
			if (iWorkingDays < iMinWorkingDays) {
				final int temp = iMinWorkingDays - iWorkingDays;
				final int temp2 = temp * 5;
				iCost += temp2;
			}

		}
		return iCost;
	}

	/**
	 * Method to calculate the penalty on curriculum compactness. Need one more
	 * parameter to make it curriculum specific
	 * 
	 * @param solution
	 *            A solution instance is send for evaluation
	 * @param curriculum
	 *            The curriculum to check penalty for
	 * 
	 * @return iCost Returns the penalty value of both soft constrains
	 */

	private int costsOnCurriculumCompactness(final ISolution solution,
			final ICurriculum curriculum) {

		int iCost = 0;
		int p, r, d, ppd;
		Set<ICourse> courses;

		currentInstance = solution.getProblemInstance();
		currentCode = solution.getCoding();
		courses = curriculum.getCourses();

		// Initial value of day and period
		ppd = currentInstance.getPeriodsPerDay();
		final int periodArray[] = new int[ppd];

		d = 1;
		p = 0;
		// Make the looping for each day
		while (d <= currentInstance.getNumberOfDays()) {
			int c = 0;
			// put default value in the array
			for (int i = 0; i < ppd; i++) {
				periodArray[i] = -1;
			}
			for (; p < ppd * d; p++) {
				for (r = 0; r < currentInstance.getNumberOfRooms(); r++) {
					// Assuming the value is null if no course is assigned
					// the course should be contained in the curriculum
					if ((currentCode[p][r] != null)
							&& courses.contains(currentCode[p][r])) {
						// Get the periods into the array to evaluate
						periodArray[c] = p;
						break;
					}
				}
				c++;
			}
			// Parse through the array for each condition:
			// 1. First period, then 2nd must be of same curriculum
			// 2. Last period, then 2nd last period must be from same curriculum
			// 3. Periods before and after must be of same curriculum
			for (int i = 0; i < ppd; i++) {
				if (periodArray[i] != -1) {
					if (i == 0) {
						if (periodArray[i + 1] == -1) {
							iCost += 2;
						}
					} else if (i == (ppd - 1)) {
						if (periodArray[i - 1] == -1) {
							iCost += 2;
						}
					} else if ((periodArray[i + 1] == -1)
							&& (periodArray[i - 1] == -1)) {
						iCost += 2;
					}
				}
			}

			// Increment to next day
			d++;
		}
		return iCost;
	}

	/**
	 * Method to calculate the penalty for room stability for entire solution
	 * 
	 * @param solution
	 *            A solution instance is send for evaluation
	 * @return iCost Returns the penalty value of both soft constrains
	 */
	private int costsOnRoomStability(final ISolution solution) {
		int iCost = 0;
		int p, r, rooms;
		Iterator<ICourse> it;
		Set<ICourse> courses;
		ICourse Course;

		currentInstance = solution.getProblemInstance();
		currentCode = solution.getCoding();
		courses = currentInstance.getCourses();
		rooms = currentInstance.getNumberOfRooms();
		final int roomArray[] = new int[rooms];

		// Iterate thru each course of problem instance
		it = courses.iterator();
		while (it.hasNext()) {
			int c = 0;
			for (int i = 0; i < rooms; i++) {
				roomArray[i] = -1;
			}
			Course = it.next();
			// iPreviousRoom = -1;
			for (p = 0; p < currentInstance.getNumberOfPeriods(); p++) {
				for (r = 0; r < currentInstance.getNumberOfRooms(); r++) {
					// Assuming the value is null if no course is assigned
					// the course should be contained in the problem instance
					if ((currentCode[p][r] != null)
							&& (currentCode[p][r] == Course)) {
						if (c == 0) {
							roomArray[c] = r;
							c++;
						} else {
							// if room is found in array don't do not add
							// penality again
							boolean bRoomFromList = false;
							for (int i = 0; i < c; i++) {
								if (roomArray[i] == r) {
									bRoomFromList = true;
									break;
								}
							}
							// add rooms to array that are not already in list
							if (!bRoomFromList) {
								roomArray[c] = r;
								c++;
							}
						}

						// there should not be another course of same
						// curriculum in different room in the same period
						break;
					}
				}

			}
			// c starts with 0 but is incremented at the last
			// reduce 1 to avoid extra penalty for first room ;-)
			iCost += (c - 1);
		}
		return iCost;
	}
}
