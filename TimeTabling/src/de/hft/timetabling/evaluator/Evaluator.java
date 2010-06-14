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

	// private ICourse currentCourseDetails;

	/**
	 * This method calculates the penalty in Room Capacity. Needs to be based on
	 * curriculum to have separate penalty points for each
	 * 
	 * @param solution
	 *            A solution instance is send for evaluation
	 * @return iCost Returns the penalty value
	 */
	private int costsOnRoomCapacity(ISolution solution, ICurriculum curriculum) {
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
					String str = ":" + currentCode[p][r].toString() + ","
							+ currentRoom.toString() + ";";
					if (!strArrayRoomCourse.contains(str)) {

						iNoOfStudents = currentCode[p][r].getNumberOfStudents();

						currentRoom = currentInstance.getRoomByUniqueNumber(r);
						iRoomCapacity = currentRoom.getCapacity();

						strArrayRoomCourse = strArrayRoomCourse + ":"
								+ currentCode[p][r].toString() + ","
								+ currentRoom.toString() + ";";

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
	 * @return iCost Returns the penalty value
	 */
	private int costsOnMinWorkingDays(ISolution solution, ICurriculum curriculum) {
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
				int temp = iMinWorkingDays - iWorkingDays;
				int temp2 = temp * 5;
				iCost += temp2;
			}

		}
		return iCost;
	}

	/**
	 * Method to calculate the penalty on curriculum compactness and room
	 * stability Need one more parameter to make it curriculum specific
	 * 
	 * @param solution
	 *            A solution instance is send for evaluation
	 * @return iCost Returns the penalty value of both soft constrains
	 */
	private int costsOnRoomStability(ISolution solution, ICurriculum curriculum) {
		int iCost = 0;
		int p, r, rooms;
		Iterator<ICourse> it;
		Set<ICourse> courses;
		ICourse Course;

		currentInstance = solution.getProblemInstance();
		currentCode = solution.getCoding();
		courses = curriculum.getCourses();
		rooms = currentInstance.getNumberOfRooms();
		int roomArray[] = new int[rooms];

		// Initial value of day and period
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
					// the course should be contained in the curriculum
					if ((currentCode[p][r] != null)
							&& (currentCode[p][r] == Course)) {
						// if (iPreviousRoom == -1) {
						// iPreviousRoom = r;
						// there should not be another course of same
						// curriculum in different room in the same period
						// it should also not go with next if statement in
						// first run
						// break;
						// }

						// Check if not new day
						// if (p % currentInstance.getPeriodsPerDay()!= 0) {
						// if (iPreviousRoom != -1) {
						// Check if the previous period course is of the
						// same curriculum
						// if (!courses.contains(currentCode[p -
						// 1][iPreviousRoom]))
						// {

						// Penalty for Room
						// currentRoom =
						// currentInstance.getRoomByUniqueNumber(r);
						// previousRoom =
						// currentInstance.getRoomByUniqueNumber(iPreviousRoom);
						// if (currentRoom.getId() != previousRoom.getId())
						// {
						// if (r != iPreviousRoom) {
						// iCost++;
						// }
						// Assign the new previous Room and Period values
						// iPreviousRoom = r;
						// }
						if (c == 0) {
							roomArray[c] = r;
							c++;
						} else {
							boolean bRoomFromList = false;
							for (int i = 0; i < c; i++) {
								if (roomArray[i] == r) {
									bRoomFromList = true;
									break;
								}
							}
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
		ServiceLocator serviceLocator = ServiceLocator.getInstance();
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
	private void callSoftConstrainEvalutors(ISolutionTableService solutionTable) {
		Set<ICurriculum> currentCurriculumSet;
		ICurriculum currentCurricula;
		int iFairness;
		List<Integer> curriculumCosts = new ArrayList<Integer>();

		int iPenalty = 0;
		int iCurriculumBasedPenalty = 0;
		int iCurriculumCompactness = 0;
		int iRoom = 0;
		int iMinWDays = 0;
		int iCompRoomS = 0;
		int iNewRoom = 0;
		int iNewMinCost = 0;
		int iCompact = 0;
		int iRoomStability = 0;

		List<ISolution> notVotedSolutions = solutionTable
				.getNotVotedSolutions();
		int nrNotVoted = notVotedSolutions.size();
		for (int i = 0; i < nrNotVoted; i++) {
			ISolution solutionCode = notVotedSolutions.get(i);
			currentInstance = solutionCode.getProblemInstance();
			currentCode = solutionCode.getCoding();
			currentCurriculumSet = currentInstance.getCurricula();
			Iterator<ICurriculum> it = currentCurriculumSet.iterator();
			iPenalty = 0;
			iRoom = 0;
			iMinWDays = 0;
			iCompRoomS = 0;
			iCompact = 0;

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
				iRoom += costsOnRoomCapacity(solutionCode, currentCurricula);
				iMinWDays += costsOnMinWorkingDays(solutionCode,
						currentCurricula);
				iCompRoomS += costsOnRoomStability(solutionCode,
						currentCurricula);
				iCompact += costsOnCurriculumCompactness(solutionCode,
						currentCurricula);
			}
			iNewRoom += costsOnRoomCapacity(solutionCode);
			iNewMinCost += costsOnMinWorkingDays(solutionCode);
			// iCurriculumCompactness
			iRoomStability += costsOnRoomStability(solutionCode);

			iPenalty = iNewRoom + iNewMinCost + iCurriculumCompactness
					+ iRoomStability;
			iFairness = evaluateFairness(curriculumCosts);

			solutionTable.voteForSolution(i, iPenalty, iFairness);
			System.out.println("RoomCapacity: " + iRoom);
			System.out.println("MinWorking day: " + iMinWDays);
			System.out.println("CirriculumRoom: " + iCompRoomS);
			System.out.println("NewRoomC: " + iNewRoom);
			System.out.println("NewMinWCost: " + iNewMinCost);
			System.out.println("NewcompactCost: " + iCompact);
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
	public int evaluateFairness(List<Integer> curriculumCosts) {
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
	public int evaluateSolution(ISolution newSolution) {
		Set<ICurriculum> currentCurriculumSet;
		ICurriculum currentCurricula;
		int numberOfCurriculum;
		int[] curriculumCosts = null;
		currentInstance = newSolution.getProblemInstance();
		currentCode = newSolution.getCoding();
		currentCurriculumSet = currentInstance.getCurricula();
		numberOfCurriculum = currentInstance.getNumberOfCurricula();
		curriculumCosts = new int[numberOfCurriculum];
		Iterator<ICurriculum> it = currentCurriculumSet.iterator();
		int c = 0;
		int iPenalty = 0;
		// int iRoom = 0;
		// int iMinWDays = 0;
		// int iCompRoomS = 0;
		// Iterate through each curriculum
		while (it.hasNext()) {
			currentCurricula = it.next();
			// Penalty calculation for given solution
			iPenalty += costsOnRoomCapacity(newSolution, currentCurricula);
			iPenalty += costsOnMinWorkingDays(newSolution, currentCurricula);
			iPenalty += costsOnCurriculumCompactness(newSolution,
					currentCurricula);
			iPenalty += costsOnRoomStability(newSolution, currentCurricula);
			curriculumCosts[c] = iPenalty;
			c++;

			// Debug code
			// iRoom += costsOnRoomCapacity(newSolution, currentCurricula);
			// iMinWDays += costsOnMinWorkingDays(newSolution,
			// currentCurricula);
			// iCompRoomS += costsOnCurriculumCompactnessAndRoomStability(
			// newSolution, currentCurricula);
		}

		// evaluateFairness(curriculumCosts, numberOfCurriculum);

		// solutionTable.voteForSolution(i, iPenalty, iFairness);
		// System.out.println("RoomCapacity: " + iRoom);
		// System.out.println("MinWorking day: " + iMinWDays);
		// System.out.println("CirriculumRoom: " + iCompRoomS);

		// evaluateFairness(curriculumCosts, numberOfCurriculum);

		return iPenalty;
	}

	/*
	 * private void callEvalutorToCheckNewPenalty(ISolution newSolution){
	 * Set<ICurriculum> currentCurriculumSet; ICurriculum currentCurricula; int
	 * numberOfCurriculum; int[] curriculumCosts = null; currentInstance =
	 * newSolution.getProblemInstance(); currentCode = newSolution.getCoding();
	 * currentCurriculumSet = currentInstance.getCurricula(); numberOfCurriculum
	 * = currentInstance.getNumberOfCurricula(); curriculumCosts = new
	 * int[numberOfCurriculum]; Iterator<ICurriculum> it =
	 * currentCurriculumSet.iterator(); int c = 0; int iPenalty = 0; int iRoom =
	 * 0; int iMinWDays = 0; int iCompRoomS = 0; // Iterate through each
	 * curriculum while (it.hasNext()) { currentCurricula = it.next(); //
	 * Penalty calculation for given solution iPenalty +=
	 * costsOnRoomCapacity(newSolution, currentCurricula); iPenalty +=
	 * costsOnMinWorkingDays(newSolution, currentCurricula); iPenalty +=
	 * costsOnCurriculumCompactnessAndRoomStability( newSolution,
	 * currentCurricula); curriculumCosts[c] = iPenalty; c++;
	 * 
	 * // Debug code iRoom += costsOnRoomCapacity(newSolution,
	 * currentCurricula); iMinWDays += costsOnMinWorkingDays(newSolution,
	 * currentCurricula); iCompRoomS +=
	 * costsOnCurriculumCompactnessAndRoomStability( newSolution,
	 * currentCurricula); }
	 * 
	 * //evaluateFairness(curriculumCosts, numberOfCurriculum);
	 * 
	 * //solutionTable.voteForSolution(i, iPenalty, iFairness);
	 * System.out.println("RoomCapacity: " + iRoom);
	 * System.out.println("MinWorking day: " + iMinWDays);
	 * System.out.println("CirriculumRoom: " + iCompRoomS); }
	 */

	private int costsOnRoomCapacity(ISolution solution) {
		int iCost = 0;
		int iNoOfStudents, iRoomCapacity;
		int r, p;
		// String strArrayRoomCourse = "";
		// Set<ICourse> courses;

		// Get the problem instance to get the values related to it
		currentInstance = solution.getProblemInstance();
		// Get the solution array for this ISolution
		currentCode = solution.getCoding();

		// Get the courses for the curriculum
		// courses = currentInstance.getCourses();

		for (p = 0; p < currentInstance.getNumberOfPeriods(); p++) {
			for (r = 0; r < currentInstance.getNumberOfRooms(); r++) {
				// Assuming the value is null if no course is assigned
				// the course should be contained in the curriculum
				if ((currentCode[p][r] != null)) {
					// currentRoom = currentInstance.getRoomByUniqueNumber(r);
					// String str = ":" + currentCode[p][r].toString() + ","
					// + currentRoom.toString() + ";";
					// if (!strArrayRoomCourse.contains(str)) {

					iNoOfStudents = currentCode[p][r].getNumberOfStudents();

					currentRoom = currentInstance.getRoomByUniqueNumber(r);
					iRoomCapacity = currentRoom.getCapacity();

					// strArrayRoomCourse = strArrayRoomCourse + ":"
					// + currentCode[p][r].toString() + ","
					// + currentRoom.toString() + ";";

					// Each student above the capacity counts as 1 point of
					// penalty
					if (iNoOfStudents > iRoomCapacity) {
						iCost = iCost + iNoOfStudents - iRoomCapacity;
					}

					// There are no more courses in the same period but
					// different room
					// break;
					// }
				}
			}

		}

		return iCost;
	}

	private int costsOnMinWorkingDays(ISolution solution) {
		int iCost = 0;
		int p, r, iWorkingDays;
		int iMinWorkingDays, iPeriodPerDay;
		Set<ICourse> courses;
		// String ArrayCourse[];
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
				int temp = iMinWorkingDays - iWorkingDays;
				int temp2 = temp * 5;
				iCost += temp2;
			}

		}
		return iCost;
	}

	private int costsOnCurriculumCompactness(ISolution solution,
			ICurriculum curriculum) {
		int iCost = 0;
		int p, r, d, ppd;
		// int iPreviousPeriod;
		Set<ICourse> courses;

		currentInstance = solution.getProblemInstance();
		currentCode = solution.getCoding();
		courses = curriculum.getCourses();

		// Initial value of day and period
		ppd = currentInstance.getPeriodsPerDay();
		int periodArray[] = new int[ppd];

		d = 1;
		p = 0;
		// Make the looping for each day
		while (d <= currentInstance.getNumberOfDays()) {
			// iPreviousPeriod = -1;
			int c = 0;
			for (int i = 0; i < ppd; i++) {
				periodArray[i] = -1;
			}
			for (; p < ppd * d; p++) {
				for (r = 0; r < currentInstance.getNumberOfRooms(); r++) {
					// Assuming the value is null if no course is assigned
					// the course should be contained in the curriculum
					/*
					 * if ((currentCode[p][r] != null) &&
					 * courses.contains(currentCode[p][r])) { if
					 * ((iPreviousPeriod == -1)) { iPreviousPeriod = p; // there
					 * should not be another course of same // curriculum in
					 * different room in the same period // it should also not
					 * go with next if statement in // first run break; //
					 * continue; }
					 * 
					 * // Check if not new day // if (p %
					 * currentInstance.getPeriodsPerDay()!= 0) { else if
					 * ((iPreviousPeriod != -1)) { // Check if the previous
					 * period course is of the // same curriculum // if
					 * (!courses.contains(currentCode[p - // 1][iPreviousRoom]))
					 * // { if (iPreviousPeriod != (p - 1)) { iCost += 2; } //
					 * Penalty for Room // currentRoom = //
					 * currentInstance.getRoomByUniqueNumber(r); // previousRoom
					 * = //
					 * currentInstance.getRoomByUniqueNumber(iPreviousRoom); //
					 * if (currentRoom.getId() != previousRoom.getId()) // {
					 * 
					 * // Assign the new previous Room and Period values
					 * 
					 * iPreviousPeriod = p; } // there should not be another
					 * course of same // curriculum in different room in the
					 * same period break; }
					 */
					// Get the periods in array to evaluate
					if ((currentCode[p][r] != null)
							&& courses.contains(currentCode[p][r])) {
						periodArray[c] = p;
						break;
					}
				}
				c++;
			}
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

	private int costsOnRoomStability(ISolution solution) {
		int iCost = 0;
		int p, r, rooms;
		Iterator<ICourse> it;
		Set<ICourse> courses;
		ICourse Course;

		currentInstance = solution.getProblemInstance();
		currentCode = solution.getCoding();
		courses = currentInstance.getCourses();
		rooms = currentInstance.getNumberOfRooms();
		int roomArray[] = new int[rooms];

		// Initial value of day and period
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
					// the course should be contained in the curriculum
					if ((currentCode[p][r] != null)
							&& (currentCode[p][r] == Course)) {
						// if (iPreviousRoom == -1) {
						// iPreviousRoom = r;
						// there should not be another course of same
						// curriculum in different room in the same period
						// it should also not go with next if statement in
						// first run
						// break;
						// }

						// Check if not new day
						// if (p % currentInstance.getPeriodsPerDay()!= 0) {
						// if (iPreviousRoom != -1) {
						// Check if the previous period course is of the
						// same curriculum
						// if (!courses.contains(currentCode[p -
						// 1][iPreviousRoom]))
						// {

						// Penalty for Room
						// currentRoom =
						// currentInstance.getRoomByUniqueNumber(r);
						// previousRoom =
						// currentInstance.getRoomByUniqueNumber(iPreviousRoom);
						// if (currentRoom.getId() != previousRoom.getId())
						// {
						// if (r != iPreviousRoom) {
						// iCost++;
						// }
						// Assign the new previous Room and Period values
						// iPreviousRoom = r;
						// }
						if (c == 0) {
							roomArray[c] = r;
							c++;
						} else {
							boolean bRoomFromList = false;
							for (int i = 0; i < c; i++) {
								if (roomArray[i] == r) {
									bRoomFromList = true;
									break;
								}
							}
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
}
